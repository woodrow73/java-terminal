package com.bennavetta.jconsole.util;

import java.awt.Color;

public class Colors {
    public static final Color
            shadowBlue = Color.decode("#7D8CA3"),
            cream = Color.decode("#FFFECB"),
            mintCream = Color.decode("#F0F7EE"),
            magicMint = Color.decode("#ACFCD9"),
            darkMagicMint = ColorUtil.adjustBrightness(magicMint, .76),
            fluorescentBlue = Color.decode("#34f6f2"),
            darkFluorescentBlue = ColorUtil.adjustBrightness(fluorescentBlue, .493),
            pink = Color.decode("#FFC4D1"),
            aeroBlue = Color.decode("#C1F7DC"),
            orchidPink = Color.decode("#F6C0D0"),
            carnationPink = Color.decode("#FF99C9"),
            pinkLace = Color.decode("#FFD5FF"),
            eerieBlack = Color.decode("#191919"),
            richBlack = Color.decode("#042A2B"),
            richerBlack = ColorUtil.adjustBrightness(Colors.richBlack, .4),
            imperialRed = Color.decode("#FF0035"),
            emerald = Color.decode("#45CB85"),
            twilightLavender = Color.decode("#824670"),
            brownSugar = Color.decode("#9E6240"),
            burlyWood = Color.decode("#D7B377"),
            blond = Color.decode("#FDF5BF"),
            darkBlond = ColorUtil.adjustBrightness(blond, .82),
            russianGreen = Color.decode("#629460");

}
