# Biobank thick client manual installation

Please follow these instructions to manually install the Biobank thick client. These instructions
apply for the following operating systems: Microsoft Windows, Apple Mac OSX, and Linux. Also, ensure
that you use the correct version for your operating system if you are running the 64 bit version of
the operating system.

**_If you have not downloaded the client software file for your operating system, please go back
to the previous document and download the client by selecting the appropriate link._**

1. Create a directory to hold the contents of the client software. This directory is referred to
as the **client directory** below.

   *For example, on Windows this can be `C:\Users\_userid_\BioBank`, where `_userid_` is your
   Windows user id. On Linux this can be `/opt/BioBank`.*

2. Move the downloaded client software file to your client directory and extract it (the client
 software file is a ZIP archive). After unzipping, your client directory should have a `BioBank` sub
 directory.

   *I.e. on Windows you will now have the `C:\Users\_userid_\BioBank\BioBank`.  On Linux you will
   have `/opt/BioBank/BioBank`.*

3. This step depends on the operating system you are using.

  1. If you are installing on MS Windows, download the following ZIP archive file and put it in the
     `BioBank` sub directory.

    http://aicml-med.cs.ualberta.ca/CBSR/jre.win32.x86.zip

    Extract `jre.win32.x86.zip` into the `BioBank` sub directory.

    *After unzipping, you should have the following directory:
     `C:\Users\_userid_\BioBank\BioBank\jre`.*

  2. Mac and Linux users need to install a Java 6 runtime environment that corresponds to their
  operating system. Please see the following web page for download instructions:
  [Java SE Runtime Environment 6 Downloads](http://www.oracle.com/technetwork/java/javase/downloads/jre6-downloads-1637595.html).

    If you have a different version of the Java JRE installed on your system, you can install the
    Java 6 JRE to the `jre` folder of the installation directory. I.e. on Linux it should be
    installed to `/opt/BioBank/BioBank/jre`.

The Biobank client application is now installed and ready to run.

* *Windows users can open Windows Explorer and go to the` C:\Users\_userid_\BioBank\BioBank`
 directory and double click on BioBank.exe to run the application.*

* *Linux users can run the application by typing the following command into the command line:
`/opt/BioBank/BioBank/BioBank &`.*

**_If you are using MS Windows, and wish to install the client in the `C:\Program Files` directory, you
 will need administrator rights to perform these steps._**

****

[Back to parent document](client_installation.md)

[Back to top](../README.md)
