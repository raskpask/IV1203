import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Hiddec {
    private byte[] key = null;
    private byte[] ctr = null;
    private byte[] input = null;
    private String output = null;
    private Cipher cipher;

    private void getCommands(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidAlgorithmParameterException {
        for (String arg : args) {
            String[] argument = arg.split("=");
            switch (argument[0]) {
                case "--key":
                    key = hexToByte(argument[1]);
                    break;
                case "--ctr":
                    ctr = hexToByte(argument[1]);
                    break;
                case "--input":
                    input = readInputFile(argument[1]);
                    break;
                case "--output":
                    output = argument[1];
                    break;
                default:
                    System.out.println("Error in argument name");
                    return;
            }
        }
        if (key == null || input == null || output == null) {
            System.out.println("You need to enter '--key' '--input' '--output' for the program to work.");
            return;
        }
        start();
    }

    private byte[] hexToByte(String key) {
        System.out.println(key);
        int len = key.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(key.charAt(i), 16) << 4) + Character.digit(key.charAt(i + 1), 16));
        }
        return data;
    }

    private byte[] readInputFile(String input) throws IOException {
        File file = new File(input);
        byte[] byteArray = new byte[(int) file.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        try {
            fis.read(byteArray);
        } catch (IOException e) {
            System.out.println("The file couldn't be opened.");
        }
        fis.close();
        return byteArray;
    }

    private void start() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IOException {
        if (ctr != null) {
            byte[] encryptedKey = hash(key);
            ctr(key, encryptedKey, ctr, input, output);
        } else if (ctr == null) {
            byte[] encryptedKey = hash(key);
            ecb(key, encryptedKey, input, output);
        }
    }

    private void ctr(byte[] key, byte[] encryptedKey, byte[] ctr, byte[] input, String output)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IOException {
        int start = 0;
        int startIndex = findKey(key,input,encryptedKey,start,ctr);
        int endIndex = findEnd(input, encryptedKey, startIndex +16);
        if(startIndex == -1 || endIndex == -1){
            System.out.println("Couldn't find the hidden blob");
            return;
        }

        byte[] data = extractData(input,startIndex,endIndex,ctr,key);
        byte[] verifyData = cipher.doFinal(Arrays.copyOfRange(input, endIndex+16, endIndex+32));
        byte[] hashOfData = hash(data);
        if(Arrays.equals(verifyData, hashOfData)){
            writeToFile(data, output);
        } else {
            System.out.println("The data could not be verified.");
        }
    }

    private byte[] extractData(byte[] input, int startIndex, int endIndex, byte[] ctr, byte[] key) {
        init(ctr,key);
        decrypt(Arrays.copyOfRange(input, startIndex, startIndex +16));
        return decrypt(Arrays.copyOfRange(input, startIndex +16, endIndex))
    }

    private int findKey(byte[] key, byte[] input, byte[] encryptedKey, int start,
            byte[] ctr)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException {
        for(int i = start; i <= input.length; i += 16){
            init(ctr, key);
            byte[] decrypted = decrypt(Arrays.copyOfRange(input, i, i + 16));
            if(Arrays.equals(decrypted,encryptedKey)){
                return i;
            }
        }
        return -1;
    }

    private void ecb(byte[] key, byte[] encryptedKey, byte[] input, String output)
            throws IOException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        int start = 0;
        init(null, key);
        int startIndex = findEnd(input, encryptedKey, start);
        int endIndex = findEnd(input, encryptedKey, startIndex + 16);

        if (startIndex == -1 || endIndex == -1) {
            System.out.println("Could not find the hidden blob");
            return;
        }
        byte[] data = decrypt(Arrays.copyOfRange(input, startIndex + 16, endIndex));
        byte[] verifyData = decrypt(Arrays.copyOfRange(input, endIndex + 16, endIndex + 32));
        byte[] hashOfData = hash(data);

        if (Arrays.equals(verifyData, hashOfData)) {
            writeToFile(data, output);
        } else {
            System.out.println("The data could not be verified.");
        }
    }

    private void writeToFile(byte[] data, String output) throws IOException{
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
        } catch (Exception e) {
            System.out.println("File to write to not found");
            return;
        }
        try {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fos.close();
    }

    private int findEnd(byte[] input, byte[] encryptedKey, int start) {
        for (int i = start; i <= input.length; i += 16) {
            byte[] decrypted = decrypt(Arrays.copyOfRange(input, i, i + 16));
            if (Arrays.equals(decrypted, encryptedKey)) {
                return i;
            }
        }
        return -1;
    }

    private byte[] decrypt(byte[] input) {
        return cipher.update(input);
    }

    private void init(byte[] ctr, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException {
        if(ctr == null){
            cipher = Cipher.getInstance("AES/ECB/NoPAdding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE,secretKey);
        } else {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(ctr);
            cipher = Cipher.getInstance("AES/CTR/NoPAdding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey,iv);
        }
    }

    private byte[] hash(byte[] key) {
        MessageDigest md = null;
        try{
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        md.update(key);
        return md.digest();
    }
    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException {
        Hiddec hiddec = new Hiddec();
        try {
            hiddec.getCommands(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}