package Graph;

public class common_tools {
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
}
