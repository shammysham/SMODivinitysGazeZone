package spireMapOverhaul.zones.brokenSpace.patches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.shop.StoreRelic;
import javassist.CtBehavior;
import spireMapOverhaul.patches.ZonePatches;
import spireMapOverhaul.zones.brokenSpace.BrokenSpaceZone;

import java.util.logging.Logger;

import static spireMapOverhaul.SpireAnniversary6Mod.makeShaderPath;
import static spireMapOverhaul.util.Wiz.adp;

public class BrokenSpaceRenderPatch {
    public static ShaderProgram brokenSpaceShader;

    private static final FrameBuffer fbo;

    private static float shaderTimer = 0.0F;

    private static final Logger logger = Logger.getLogger(BrokenSpaceRenderPatch.class.getName());

    @SpirePatch(
            clz = RewardItem.class,
            method = "render"
    )
    public static class RenderBrokenSpaceRewards {
        @SpirePrefixPatch
        public static void addShader(RewardItem __instance, SpriteBatch sb) {
            if (shouldRenderBrokenSpaceShader(__instance)) {
                StartFbo(sb);
            }
        }

        @SpirePostfixPatch
        public static void removeShader(RewardItem __instance, SpriteBatch sb) {
            if (shouldRenderBrokenSpaceShader(__instance)) {
                StopFbo(sb);
            }
        }

    }


    @SpirePatch(
            clz = AbstractCard.class,
            method = "renderCard"
    )
    public static class RenderBrokenSpaceCards {
        @SpirePrefixPatch
        public static void addShader(AbstractCard __instance, SpriteBatch sb) {
            if (shouldRenderBrokenSpaceShader(__instance)) {

                StartFbo(sb);
            }
        }

        @SpirePostfixPatch
        public static void removeShader(AbstractCard __instance, SpriteBatch sb) {
            if (shouldRenderBrokenSpaceShader(__instance)) {
                float strength = 1.0F;

                if (__instance.hb.hovered) {
                    strength = 0.2F;
                }
                StopFbo(sb, strength, RandomTimeField.randomTime.get(__instance));

            }
        }
    }

    @SpirePatch(
            clz = StoreRelic.class,
            method = "render"
    )
    public static class RenderBrokenSpaceRelics {
        @SpirePrefixPatch
        public static void addShader(StoreRelic __instance, SpriteBatch sb) {
            if (shouldRenderBrokenSpaceShader(__instance.relic, true)) {
                StartFbo(sb);
            }
        }

        @SpirePostfixPatch
        public static void removeShader(StoreRelic __instance, SpriteBatch sb) {
            if (shouldRenderBrokenSpaceShader(__instance.relic, true)) {
                StopFbo(sb);
            }
        }
    }

    public static void StartFbo(SpriteBatch sb) {
        sb.end();

        fbo.begin();
        Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sb.begin();
    }

    public static void StopFbo(SpriteBatch sb, float strength) {
        StopFbo(sb, strength, 0.0F);
    }

    public static void StopFbo(SpriteBatch sb, float strength, float timerOffset) {
        sb.flush();
        fbo.end();

        TextureRegion region = new TextureRegion(fbo.getColorBufferTexture());
        region.flip(false, true);


        sb.setShader(brokenSpaceShader);
        sb.setColor(Color.WHITE);
        brokenSpaceShader.setUniformf("u_time", shaderTimer + timerOffset);
        brokenSpaceShader.setUniformf("u_strength", strength);

        sb.draw(region, 0, 0);
        sb.setShader(null);
    }

    public static void StopFbo(SpriteBatch sb) {
        StopFbo(sb, 1.0F);
    }


    private static boolean shouldRenderBrokenSpaceShader(RewardItem __instance) {
        return inBrokenSpace() && (__instance.type == RewardItem.RewardType.CARD || (__instance.type == RewardItem.RewardType.RELIC && isUnnatural(__instance.relic, false)));

    }

    private static boolean shouldRenderBrokenSpaceShader(AbstractCard __instance) {
        return inBrokenSpace() && isUnnatural(__instance) && !adp().masterDeck.contains(__instance);
    }

    private static boolean shouldRenderBrokenSpaceShader(AbstractRelic __instance, boolean isShop) {
        return inBrokenSpace() && isUnnatural(__instance, isShop) && !adp().relics.contains(__instance);
    }

    private static boolean isUnnatural(AbstractRelic __instance, boolean isShop) {
        boolean isUnnatural = __instance.tier == AbstractRelic.RelicTier.SPECIAL || __instance.tier == AbstractRelic.RelicTier.BOSS;
        if (!isShop) {
            isUnnatural = isUnnatural || __instance.tier == AbstractRelic.RelicTier.SHOP;

        }

        return isUnnatural;
    }


    private static boolean isUnnatural(AbstractCard __instance) {
        return __instance.color != adp().getCardColor() && BrokenSpaceZone.UnnaturalCardField.unnatural.get(__instance);
    }


    private static boolean inBrokenSpace() {
        return ZonePatches.currentZone() != null && ZonePatches.currentZone().id.equals("BrokenSpace");
    }


    @SpirePatch(
            clz = CardCrawlGame.class,
            method = "render"
    )
    public static class UpdateShaderTimer {
        @SpirePostfixPatch
        public static void updateShaderTimer() {
            if (inBrokenSpace()) {
                shaderTimer += Gdx.graphics.getDeltaTime();
            }
        }

    }

    @SpirePatch2(
            clz = AbstractCard.class,
            method = SpirePatch.CLASS
    )
    public static class RandomTimeField {
        public static SpireField<Float> randomTime = new SpireField<>(() -> (float) Math.random() * 1000f);
    }



    static {
        brokenSpaceShader = new ShaderProgram(Gdx.files.internal(makeShaderPath("BrokenSpace/Glitch.vs")), Gdx.files.internal(makeShaderPath( "BrokenSpace/Glitch.fs")));
        if (true) {
            logger.warning("Broken Space shader: " + brokenSpaceShader.getLog());
        }
        brokenSpaceShader.begin();


        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, false);
    }
}