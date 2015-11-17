package com.io.net;

/**
 * Used from mkyong from his site and post
 * http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
 *
 * There have been modifications to this file
 */

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnZip
{
    public static void unZip(byte[] inputBytes, String outFolder) throws IOException {

        byte[] buffer = new byte[1024];

        //create output directory is not exists
        File folder = new File(outFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        //create input zip stream from byte array
        ByteArrayInputStream byteStream = new ByteArrayInputStream(inputBytes);
        ZipInputStream zis = new ZipInputStream(byteStream);

        //get the zipped file list entry
        ZipEntry ze = zis.getNextEntry();

        while (ze!=null) {

            String fileName = ze.getName();
            File newFile = new File(outFolder + File.separator + fileName);

            System.out.println("file unzip : "+ newFile.getAbsoluteFile());

            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();

            FileOutputStream fos = new FileOutputStream(newFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

    }
}