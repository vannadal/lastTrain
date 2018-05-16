package gov.bjjtw.lastTrain.CommonTools;


import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class CommonTools {
    public static int TransferTime(String time) {
        String str[]=time.split(":");
        //System.out.println(time);
        int h=Integer.parseInt(str[0]);
        if(h==0) {
            h=24;
        }

        int m=Integer.parseInt(str[1]);
        int s=Integer.parseInt(str[2]);
        return h*3600+m*60+s;
    }

    public static String SecondToTime(int second) {
        int h=second/3600;
        int m=(second-h*3600)/60;
        int s=(second-h*3600-m*60);
        return h+":"+m+":"+s;
    }

    public static Object deepCopy(Object orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }

}
