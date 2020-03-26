import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;

public class HillDecipher {
    private String cipher;
    private ArrayList<DenseVector<Real>> cipherArray;
    private ArrayList array;
    private ArrayList<Real> keyList;
    private BufferedWriter writer;
    private DenseMatrix<Real> keyMatrix, invMatrix, plainText;

    public DenseMatrix<Real> readCipher(String cipherFile, int blockSize) throws IOException {
        File file = new File(cipherFile);
        Scanner scanner = new Scanner(file);
        cipherArray = new ArrayList<>();
        array = new ArrayList<>();

        while (scanner.hasNext()) {
            cipher = String.valueOf(scanner.nextInt());
            array.add(cipher);
        }

        for (int i = 0; i < array.size(); i += blockSize) {
            keyList = new ArrayList<>();
            for (int j = 0; j < blockSize; j++) {
                int letter = Integer.parseInt(String.valueOf(array.get(i + j)));
                keyList.add(Real.valueOf(letter));
            }
            cipherArray.add(DenseVector.valueOf(keyList));
        }
        return DenseMatrix.valueOf(cipherArray).transpose();
    }

    public DenseMatrix<Real> readKey(String keyFile, int blockSize) throws IOException {
        File file = new File(keyFile);
        Scanner scanner = new Scanner(file);
        Real[][] matrixArray = new Real[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                matrixArray[i][j] = Real.valueOf(scanner.nextInt());
            }
        }
        keyMatrix = DenseMatrix.valueOf(matrixArray);
        return keyMatrix;
    }

    public void decode(DenseMatrix<Real> cipher, DenseMatrix<Real> keyMatrix, String plainFile, int radix,
            int blockSize) throws IOException {
        Real[][] array = new Real[keyMatrix.getNumberOfRows()][keyMatrix.getNumberOfColumns()];
        ArrayList temp = new ArrayList();

        LargeInteger determinant = LargeInteger.valueOf(keyMatrix.determinant().longValue());
        Real invDeterminant = Real.valueOf(determinant.modInverse(LargeInteger.valueOf(radix)).longValue());
        invMatrix = keyMatrix.inverse().times(Real.valueOf(determinant.longValue()).times(invDeterminant));
        for (int i = 0; i < invMatrix.getNumberOfRows(); i++) {
            for (int j = 0; j < invMatrix.getNumberOfColumns(); j++) {
                LargeInteger mod = LargeInteger.valueOf(invMatrix.get(i, j).longValue())
                        .mod(LargeInteger.valueOf(radix));
                array[i][j] = Real.valueOf(mod.longValue());
            }
        }
        DenseMatrix<Real> decryptKey = DenseMatrix.valueOf(array);
        plainText = decryptKey.times(cipher).transpose();
        for (int i = 0; i < plainText.getNumberOfRows(); i++) {
            for (int j = 0; j < plainText.getNumberOfColumns(); j++) {
                temp.add(String.valueOf((plainText.get(i, j).intValue()) % radix));
            }
        }
        removePadding(temp, plainFile, blockSize);
    }

    public void removePadding(ArrayList text, String plainFile, int blockSize) throws IOException {
        writer = new BufferedWriter(new FileWriter(plainFile));
        int lastTextIndex = text.size() - 1;
        int lastInt = Integer.parseInt(String.valueOf(text.get(lastTextIndex)));
        for (int i = 0; i < lastInt; i++) {
            text.remove(lastTextIndex - i);
        }

        for (int i = 0; i < text.size(); i++) {
            writer.write(String.valueOf(text.get(i)));
            writer.write(" ");
        }
        writer.close();
    }

    public static void main(String[] args) {
        HillDecipher hillDecipher = new HillDecipher();
        DenseMatrix cipher;
        if (Integer.parseInt(args[0]) > 256 || Integer.parseInt(args[1]) > 4) {
            System.out.println("Radix or blocksize is to big");
            return;
        } else {
            try {
                cipher = hillDecipher.readCipher(args[4], Integer.parseInt(args[1]));
            } catch (Exception e) {
                System.out.println("The cipher could not be found or read.");
                return;
            }
            DenseMatrix<Real> key;
            try {
                key = hillDecipher.readKey(args[2], Integer.parseInt(args[1]));
            } catch (Exception e) {
                System.out.println("The key could not be found or read.");
                return;
            }
            try {
                hillDecipher.decode(cipher, key, args[3], Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            } catch (Exception e) {
                System.out.println("The message could not be decoded.");
                return;
            }

        }
    }
}