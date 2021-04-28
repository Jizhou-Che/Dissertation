package jonto.io;

import java.io.*;

public class TXTReader {
    private BufferedReader rBuffer = null;

    public TXTReader(InputStream isr_file) {
        try {
            rBuffer = new BufferedReader(new InputStreamReader(isr_file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TXTReader(String filestr) throws FileNotFoundException {
        File file = new File(filestr);

        if (file.exists()) {
            try {
                rBuffer = new BufferedReader(new FileReader(filestr));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("The file '" + filestr + "' doesn't exist.");
        }
    }

    public TXTReader(File file) throws FileNotFoundException {
        if (file.exists()) {
            try {
                rBuffer = new BufferedReader(new FileReader(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("The file '" + file.getAbsolutePath() + "' doesn't exist.");
        }
    }

    public String readLine() {
        try {
            return rBuffer.readLine();
        } catch (Exception e) {
            System.err.println("An error occurred reading the file: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    public void closeBuffer() {
        try {
            rBuffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
