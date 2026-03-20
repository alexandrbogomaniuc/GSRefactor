import com.abs.casino.common.util.string.StringUtils;
import com.google.common.base.Splitter;

import java.io.FileInputStream;


/**
 * User: flsh
 * Date: 14.08.14.
 */
public class TestParser {
    public static void main(String[] args) throws Exception {
        //String md5 = StringUtils.getMD5("1262104172862500jMR8dc01q2DwdFUI8");
        String md5 = StringUtils.getMD5("4002504157031277jMR8dc01q2DwdFUI8");
        System.out.println(md5);
        if(true) return;

        //FileInputStream fr = new FileInputStream("/home/anaz/prj/bitcoin-flappy-atthecopa.log");
        FileInputStream fr = new FileInputStream("/home/anaz/prj/bitcoin_thebud2.log");
        String s = StringUtils.getStreamAsString(fr);
        //System.out.println(s);
        Iterable<String> iterable = Splitter.on(" time:").split(s);
        long sumBet = 0, sumWin = 0;
        long prevBalance = 655492;
        for (String s1 : iterable) {
            //System.out.println("=" + s1);
            int beginIndex = s1.indexOf("<EXTSYSTEM>");
            if(beginIndex < 0) {
                //System.out.println("=" + s1);
                continue;
            }
            String s2 = s1.substring(beginIndex);
            System.out.println(s2);

            long balance = 0;
            int balanceIndex = s2.indexOf("<BALANCE>");
            if(balanceIndex > 0) {
                String s3 = s2.substring(balanceIndex + 9, s2.indexOf("</BALANCE>"));
                System.out.println("balance=" + s3);
                balance = Long.parseLong(s3);
            }

            int betIndex = s2.indexOf("<BET>");
            if(betIndex > 0) {
                String s3 = s2.substring(betIndex + 5, s2.indexOf("|"));
                System.out.println("bet=" + s3);
                long bet = Long.parseLong(s3);
                sumBet += bet;
                if(prevBalance - bet != balance) {
                    System.out.println("Balance mismatch: " + (balance - prevBalance));
                }
            }

            int winIndex = s2.indexOf("<WIN>");
            if(winIndex > 0) {
                String s3 = s2.substring(winIndex + 5, s2.indexOf("|"));
                System.out.println("win=" + s3);
                long win = Long.parseLong(s3);
                sumWin += win;
                if(prevBalance + win != balance) {
                    System.out.println("Balance mismatch: " + (balance - prevBalance));
                }
            }
            prevBalance = balance;

            System.out.println("");
        }
        System.out.println("sumBet=" + sumBet + ", sumWin=" + sumWin + ", diff=" + (sumBet-sumWin));
    }
}
