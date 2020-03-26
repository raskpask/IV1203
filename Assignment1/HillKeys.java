import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.Matrix;

public class HillKeys {
    private Random random;
    private Matrix<Real> key;
    private BufferedWriter writer;

    private Matrix<Real> generateKey(int radix, int blocksize) {
        Real[][] matrixArray = new Real[blocksize][blocksize];
        random = new Random();
        while (true) {
            for (int i = 0; i < blocksize; i++) {
                for (int j = 0; j < blocksize; j++) {
                    matrixArray[i][j] = Real.valueOf(random.nextInt(radix));
                }
            }
            key = DenseMatrix.valueOf(matrixArray);
            LargeInteger determinant = LargeInteger.valueOf(key.determinant().longValue());
            if ((key.determinant() != Real.valueOf(0))
                    && (determinant.gcd(LargeInteger.valueOf(radix)).equals(LargeInteger.valueOf(1)))
                    && key.getNumberOfRows() == key.getNumberOfColumns()) {
                break;
            }

        }
        return key;
    }

    public void writeToFile(Matrix keyMatrix, String keyFile) {
        try {
            File file = new File(keyFile);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(keyMatrix.toString().replaceAll("[{,}]", ""));
            writer.close();
        } catch (Exception e) {
            System.out.println("The key could not be written to the file.");
        }
    }

    public static void main(String[] args) {
        if (Integer.parseInt(args[0]) > 256 || Integer.parseInt(args[1]) > 9) {
            System.out.println("Radix or blocksize is to big");
        } else {
            HillKeys hillKeys = new HillKeys();
            Matrix key = hillKeys.generateKey(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            try {
                hillKeys.writeToFile(key, args[2]);
            } catch (Exception e) {
                System.out.println("The key could not be written to the file.");
                return;
            }

        }
    }
}