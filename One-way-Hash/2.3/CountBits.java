public class CountBits{
    private static int diff = 0;

    public static void main(String[] args){
        // String string1 = "05da06e28ed1a947f60cc1cc4788cb98"; // MD5 before
        // String string2 = "dc0809eae8f29e4867f2f2a63d0d8604"; // MD5 bitflip
        String string1 = "14b4388098465cb0e4735678d92bfb420ff990586b2e890712f1a8e6c5928c4c"; // after bitflip
        String string2 = "2190179002f34b769610c1a3254a7ebfddd24fcca103c3371cc004c99f3ed5ea"; // original
        byte[] array1 = string1.getBytes();
        byte[] array2 = string2.getBytes();

        for(int i = 0; i < array1.length; i++ ){
            if(array1[i] == array2[i]){
                diff++;
            }
        }
        System.out.println(diff);
    }
}