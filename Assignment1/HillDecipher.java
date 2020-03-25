import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class HillDecipher {
    private String cipher;
    private ArrayList<DenseVector<Real>> cipherArray;
    private ArrayList<> array;
    private ArrayList<Real> keyList;

    public DenseMatrix<Real> readCipher(String cipherFile, int blockSize){
        File file = new File(cipherFile);
        Scanner scanner = new Scanner(file);
        cipherArray= new ArrayList<>();
        array = new ArrayList<>();

        while(scanner.hasNext()){
            cipher = String.valueOf(scanner.nextInt());
            array.add(cipher);
        }

        for (int i =0; i <array.size(); i += blockSize){
            keyList = new ArrayList<>();
            for(int j =0; j < blockSize; j++){
                int letter = Integer.parseInt(String.valueOf(array.get(i+j)));
                keyList.add(Real.valueOf(letter));
            }
            cipherArray.add(DenseVector.valueOf(keyList));
        }
        return DenseMatrix.valueOf(cipherArray).transpose();
    }
    public DenseMatrix<Real> readKey(String keyFile,int blockSize){
        File file = new File(keyFile);
        Scanner scanner = new Scanner(file);
        Real[][] matrixArray = new Real[blockSize][blockSize];
        for(int i = 0; i < blockSize; i++){
            for (int j = 0; j < blockSize; j++){
                matrixArray[i][j] = Real.valueOf(scanner.nextInt());
            }
        }
        keyMatrix = DenseMatrix.valueOf(matrixArray);
        return keyMatrix;
    }

    public void decode(DenseMatrix<Real> cipher, DenseMatrix<Real> keyMatrix,String plainFile,int radix){
        Real[][] array = new Real[keyMatrix.getNumberOfRows()][keyMatrix.getNumberOfColumns];
        LinkedList temp = new LinkedList<>();

        LargeInteger determinant = LargeInteger.valueOf(keyMatrix.determinant().longValue());
        Real invDeterminant = Real.valueOf(determinant.modInverse(LargeInteger.valueOf(radix)).longValue());
        invMatrix = keyMatrix.inverse().times(Real.valueOf(determinant.longValue()).times(incDet));
    }
}