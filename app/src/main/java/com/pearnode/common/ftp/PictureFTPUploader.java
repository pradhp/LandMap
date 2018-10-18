package com.pearnode.common.ftp;

import com.pearnode.constants.FixedValuesRegistry;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by USER on 2/21/2018.
 */
public class PictureFTPUploader {

    private String picName = null;
    private File picFile = null;

    public PictureFTPUploader(String picName, File picFile){
        this.picName = picName;
        this.picFile = picFile;
    }

    public String doUpload(){
        String uploadFilePath = null;
        try {
            String rootDir = "/resources";
            String appsDir = rootDir + "/apps/" + FixedValuesRegistry.APP_NAME + "/";
            String imagesDirectory = appsDir + "/pictures/";

            FTPClient ftpClient = new FTPClient();
            ftpClient.setDefaultTimeout(5 * 60 * 1000); // 5 mins

            ftpClient.connect(FixedValuesRegistry.FTP_HOST, 12996);
            ftpClient.login("pnftp", "pnftp987126");

            ftpCreateDirectoryTree(ftpClient, imagesDirectory);
            ftpClient.changeWorkingDirectory(imagesDirectory);

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            if(picFile.exists()){
                System.out.println("Picture to be uploaded [" + picFile.getAbsolutePath() + "] with size [" + picFile.length() + "]");
            }

            BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(picFile));
            uploadFilePath = appsDir + picName;
            try{
                ftpClient.changeWorkingDirectory(imagesDirectory);
                ftpClient.deleteFile(picName);
                ftpClient.storeFile(picName, buffIn);
            }catch (Exception e){
            }
            buffIn.close();

            ftpClient.logout();
            ftpClient.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        return uploadFilePath;
    }

    /**
     * utility to create an arbitrary directory hierarchy on the remote ftp server
     * @param client
     * @param dirTree  the directory tree only delimited with / chars.  No file name!
     * @throws Exception
     */
    private static void ftpCreateDirectoryTree( FTPClient client, String dirTree ) throws IOException {

        boolean dirExists = true;

        //tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
        String[] directories = dirTree.split("/");
        for (String dir : directories ) {
            if (!dir.isEmpty() ) {
                dirExists = client.changeWorkingDirectory(dir);
                if (!dirExists) {
                    if (!client.makeDirectory(dir)) {
                        throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
                    }
                    if (!client.changeWorkingDirectory(dir)) {
                        throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
                    }
                }
            }
        }
    }
}
