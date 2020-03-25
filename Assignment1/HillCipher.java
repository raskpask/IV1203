import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;

public class HillCipher {
    private ArrayList<Integer> charArray;
    private BufferedWriter writer;
    private Scanner scanner;
    
    public void readMessage(String filename, int blocksize ) throws FileNotFoundException {
        File file = new File(filename);
        scanner = new Scanner(file);
        charArray = new ArrayList<Integer>();

        String message;
        while(scanner.hasNext()){
            message = scanner.next();
            for(int i = 0; i < message.length(); i++){
                charArray.add(Integer.valueOf(message.charAt(i)));
                System.out.println(Integer.valueOf(message.charAt(i))%26);
            }
        }
        addPadding(charArray,blocksize);
    }

    public void addPadding(ArrayList<Integer> charArray, int blocksize){
        // int end = blocksize - (charArray.size() % blocksize);
        int size = charArray.size() + ( blocksize - (charArray.size() % blocksize));
        for (int i = charArray.size(); i< size; i++){
            charArray.add(i);
        } 
    }
    
    public void encode(int blocksize,String key,String cipher, int radix) throws IOException {
        File file = new File(key);
        try {
            writer = new BufferedWriter(new FileWriter(cipher));
        } catch (IOException e){
            System.out.println("The text could not be found.");
        }
        int[][] keyMatrix = new int[blocksize][blocksize];

        try (Scanner scanner = new Scanner(file)){
            for(int i = 0; i< blocksize; i++){
                for(int j =0; j < blocksize; j++){
                    keyMatrix[i][j] = scanner.nextInt();
                }
            }
        } catch (Exception e) {
            System.out.println("The key file could not be found.");
        }
        for (int i = 0; i< charArray.size(); i += keyMatrix.length){
            for(int j = 0; j< keyMatrix.length; j++){
                int encoded = 0;
                for (int k = 0; k < keyMatrix.length; k++){
                    encoded += (charArray.get(k+i))* keyMatrix[j][k];
                }
                try{
                    writer.write(String.valueOf(encoded % radix)); 
                    writer.write(" ");
                } catch (Exception e){
                    System.out.println("The text could not be written to the file.");
                }
            }
        }
        System.out.println("Done");
        writer.close();
    }
    public static void main(String[] args){
        if(Integer.parseInt(args[0]) > 256 || Integer.parseInt(args[1]) > 4) {
            System.out.println("Radix or blocksize is to big");
        } else {
            HillCipher hillCipher = new HillCipher();
            try  {
                hillCipher.readMessage(args[3],Integer.parseInt(args[1]));
            } catch (Exception e){
                System.out.println("The plaintext file couldn't be opened.");
                return;
            }
            try{
                hillCipher.encode(Integer.parseInt(args[1]),args[2],args[4], Integer.parseInt(args[0]));
            } catch (Exception e){
                System.out.println("The message could not be coded.");
                return;
            }
        }
    }
}
