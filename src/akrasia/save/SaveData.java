package akrasia.save;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SaveData {
    public SaveData() {
    }

    public static String MD5Hash(String filename){
        try{
            InputStream fis = new FileInputStream("filename");
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int numRead;
            do{
                numRead = fis.read(buffer);
                    if(numRead >  0){
                        digest.update(buffer, 0, numRead);
                    }
            } while (numRead != -1);
            fis.close();

            buffer = digest.digest();
            String result = "";
            for(int i = 0; i < buffer.length; i++){
                result +=
                          Integer.toString( ( buffer[i] & 0xff ) + 0x100, 16).substring( 1 );
            }

            return result;
        }
        catch(NoSuchAlgorithmException exception){
        }
        catch(FileNotFoundException exception){
        }
        catch(IOException exception){
        }

        return "failed";
    }
}
