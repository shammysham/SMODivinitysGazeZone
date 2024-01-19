/*
    code copied from storm zone
 */

package spireMapOverhaul.zones.windy.patches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.scenes.AbstractScene;
import javassist.*;
import org.clapper.util.classutil.*;
import spireMapOverhaul.zones.storm.StormUtil;
import spireMapOverhaul.zones.windy.WindyZone;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static spireMapOverhaul.SpireAnniversary6Mod.makeShaderPath;
import static spireMapOverhaul.SpireAnniversary6Mod.time;
import static spireMapOverhaul.util.Wiz.getCurZone;
import static spireMapOverhaul.zones.storm.StormZone.RAIN_KEY;

public class WindyAudioPatch {
    public static boolean inWindyZone() {
            return getCurZone() instanceof WindyZone;
        }

    @SpirePatch(clz = AbstractScene.class, method = "updateAmbienceVolume")
    public static class SetWindAmbiance {

        @SpirePrefixPatch
        public static void Prefix(AbstractScene __instance) {
            if(inWindyZone()) {
                if (Settings.AMBIANCE_ON) {
                    CardCrawlGame.sound.adjustVolume(WindyZone.WINDY_KEY, WindyZone.windID);
                } else {
                    CardCrawlGame.sound.adjustVolume(WindyZone.WINDY_KEY, WindyZone.windID, 0.0f);
                }
            }
        }
    }

    @SpirePatch(clz = AbstractScene.class, method = "fadeOutAmbiance")
    public static class FadeOutAmbiencePatch {

        @SpirePostfixPatch
        public static void Postfix() {
            if (inWindyZone()) {
                CardCrawlGame.sound.adjustVolume(WindyZone.WINDY_KEY, WindyZone.windID, 0.0f);
                WindyZone.windID = 0L;
            }
        }
    }

    @SpirePatch(clz = AbstractScene.class, method = "muteAmbienceVolume")
    public static class MuteAmbiencePatch {

        @SpirePostfixPatch
        public static void Postfix() {
            if (inWindyZone()) {
                CardCrawlGame.sound.adjustVolume(WindyZone.WINDY_KEY, WindyZone.windID, 0.0f);
            }
        }
    }
}
