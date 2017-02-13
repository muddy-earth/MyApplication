package com.example.debajyotidas.myapplication;

/**
 * Created by overtatech-4 on 18/1/17.
 */

public class Constants {

    public static String /*UID,*/ PHOTO_URI, NAME;
    //public static boolean IS_USER_CREATED=false;
    public static String Reg_Token="";

    public static final String LEGACY_SERVER_KEY="AIzaSyBtfXBcIqiT2-aNCPGV8pW-R8wtIE3NzbY";
    public static final String FIREBASE_PUSH_URL="https://fcm.googleapis.com/fcm/send";

    public interface SHARED_PREFS{
        String UID="uid";
        String REG_TOKEN="reg_token";
        String NAME="###@@!@#!@#n#a55m2e%%^#^&*$";

    }
    public interface KEYS{

         interface USERS{
             String LAST_ONLINE="lastOnline";
             String ONLINE="online";
             String POINTS="points";
             //String RATING="rating";
             String REG_TOKEN="reg_token";
             String PHOTO_URL="photo_url";
             String WIN_COUNT="winCount";
             String LOOSE_COUNT="looseCount";
             String BLOCK_REQUEST_FROM_BEGINNER ="blockRequestFromBeginner";
             String BLOCK_REQUEST_FROM_MEDIUM ="blockRequestFromMedium";
             String BLOCK_REQUEST_FROM_HIGHER ="blockRequestFromHigher";
        }
    }

public interface BET{
    int BEGINNER =10;
    int MEDIUM=20;
    int HIGHER=30;
    int SAME=5;
}

    public interface INTERVAL{
        interface BEGINNER{
            //long START=0;
            long END=1000;
        }
        interface MEDIUM{
            long START=1001;
            long END=2000;
        }
        interface HIGHER{
            long START=2001;
            //long END=3000;
        }

    }

}
