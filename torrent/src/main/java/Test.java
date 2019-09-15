import java.util.*;

class Test {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        for (int j = 0; j < t; j++) {
            int n = sc.nextInt();
            String binaryString = "";
            while(n!=0) {
                int r = n%2;
                n = n/2;
                binaryString = r + binaryString;
            }

            int countOf0s[] = new int[binaryString.length()];
            int countOf1s[] = new int[binaryString.length()];

            int[] countOfOddEven0sTilli = new int[2];
            int[] countOfOddEven1sTilli = new int[2];

            for(int i=0;i<binaryString.length();i++) {
                char ch = binaryString.charAt(i);
                if(ch == '0')
                    countOf0s[i]++;
                else
                    countOf1s[i]++;
            }
            int count0s=0, count1s=0;
            for(int i=0;i<countOf0s.length;i++) {
                int valAt0i = countOf0s[i];
                int valAt1i = countOf1s[i];

                if(valAt0i%2 != 0) {
                    count0s = count0s + countOfOddEven0sTilli[0] + 1;
                    countOfOddEven0sTilli[1] ++;
                } else {
                    count0s = count0s + countOfOddEven0sTilli[1];
                    countOfOddEven0sTilli[0] ++;
                }

                if(valAt1i%2 != 0) {
                    count1s = count1s + countOfOddEven1sTilli[0] + 1;
                    countOfOddEven1sTilli[1] ++;
                } else {
                    count1s = count1s + countOfOddEven1sTilli[1];
                    countOfOddEven1sTilli[0] ++;
                }
            }
            System.out.println(count0s + " " + count1s);
        }

    }
}