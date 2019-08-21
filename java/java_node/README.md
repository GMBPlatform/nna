GMB Network Node java version 0.1.0 - James, Edward, ks.nam

# Inspection
SeongKee, Kim ( sk.quant@gmail.com)

# dependency
 - bcpkix-jdk15on-161.jar
 - bcprov-jdk15on-161.jar
 - gson-2.8.5.jar
 - netty-all-4.1.36.Final.jar
 - commons-logging-1.2.jar
 - httpclient-4.5.8.jar
 - httpCore-4.4.11.jar
 - log4j-api-.2.12.0.jar
 - log4j-core-2.12.0.jar
 - commons-lang3-3.9.jar
 - mysql-connector-java-8.0.16.jar

# Prerequisite
- Download and setting up MySQL - Our Program store the data in MySQL
- Download set_env.sh file in our project repository
- Modify the set_env.sh file to suit your server environment
- Execute set_env.sh for setting up your server

# usage
1. export  jar file
2. create dir
    dir structure
    ```
      -- Node
          戌式式conf
          戌式式key
           |     戌式me
           |          戌式privkey.pem (your secp256r1 private key)
           |          戌式pubkey.pem (your secp256r1 public key)
          戌式式yourJarFile(export jar file)
3. single test 
    ```
    $ java -jar yourJarFile.jar
    ```
4. enter each command ( commands reference Example.java )
5. If start with ISA then ISA will execute Jar File