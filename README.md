# Agoda Take Home Challenge

Write a program that compresses files and folders into a set of compressed files such that of each compressed file doesn’t exceed a maximum size. The same program can be used for decompressing the files that it has generated earlier. The output of the decompression should be identical to the original input of the compression process.

# Key Challenges and Constraints: 

 1. Input files can be very large. [More than JVM size]
 2. Compressed files generated can also be very large. 
 3. Compression output should generate less files.
 4. Support different compression algorithms
 5. Make parallel calls while compression and decompression, as much as possible.
 
## Our Approach: 

As mentioned in the problem, we use zip for compression algorithm [JDK Implementation]. However, our algorithm supports other compression algorithms as well and we use strategy design pattern for the same. [Point 4]

[Point 5]. We use Java Executor services to make parallel calls during compression and decompression. Thread Pool size is set to 20 for our application. 

# Input Validation:

Our Input and Output directories and MaxCompressedSizeThresold are validated and throws exception if the request is invalid. 

# Compression:

In the compression input request, maximum compressed size per file is given as input. It can be greater than JVM size. 

Hence, to make the best use of threads with JVM constraints, we calculate MaxCompressedSizeThreshold as follows:

MaxCompressedSizeThresold = Minimum of { MaxCompressedSizeThresold Input, JVM Max. allowed memory/Thread_Pool_Size }

[Points 1, 2, 3]: The key problem in this challenge is, how do we handle large files and also we should generate compressed less files as possible

To handle above issues, we perform 2 levels of compression : 

In the first level, if the input file is more than MaxCompressedSizeThresold, we split the given file into chunks of size MaxCompressedSizeThresold and zip it.

In the second level, in order to generate the minimum files, we perform the second level compression as follows.

We group zips of all files that sums upto MaxCompressedSizeThresold and zip it. We repeat the above process and create level 2 zips.

For Eg :  File1 (14 MB), File2(1 MB), File3 (1 MB), File4(1 MB), File5 (1 MB), File6 (1 MB)

MaxCompressedSizeThresold = 4 MB:

Level 1 output :  File1.part_0.level1.zip (4 MB), File1.part_1.level1.zip (4MB) , File1.part_2.level1.zip(4MB),
File1.part_3.level1.zip(2MB), File2.part_0.level1.zip (1MB), File3.part_0.level1.zip (1MB), File4.part_0.level1.zip (1MB), File5.part_0.level1.zip (1MB), File6.part_0.level1.zip (1MB)

Level 2 output: Level_2_part_0.level2.zip (4MB) [Grouping files 2-5], Level_2_part_1.level2.zip (3MB) [Grouping files 6 and 6th chunk of File 1], File1.part_0.level1.zip (4 MB), File1.part_1.level1.zip (4MB) , File1.part_2.level1.zip(4MB)


We have generated only 5 files as shown in level 2 output, instead of 9 in level 1 output. 

We can realize the benefit of level 2 compression with more number of input files that consists of large files and small files.

To perform level 2 compressions, we sort the output of level 1 files and then group the chunks based on the file sizes. 
The files can present in different directories and at different depth, however our algorithm chooses wisely and perform level 2 compression and generate minimum files.

# Decompression:

Here, we first perform level 2 decompressions if present, followed by level 1 decompressions. 

After level 1 decompressions, we merge the chunks that were split before by our compression algorithm and 

generates the required output files.


## Getting Started

### Git clone:

Desktop > git clone https://github.com/mkarthick19/Agoda-Compression.git

### Running the application 

cd Agoda-Compression/

# Sample Compression 

Agoda-Compression > mvn spring-boot:run 

Please enter 1 for Compression and 2 for Decompression :
1

Please enter the following:

Path to input directory :
/Users/karthickm/Documents/Agoda-Compression/InputDir/

Path to output directory :
/Users/karthickm/Compressed/

Maximum Compressed Size per file threshold :
4

# Sample Decompression 

Agoda-Compression > mvn spring-boot:run

Please enter 1 for Compression and 2 for Decompression :
2

Please enter the following:

Path to input directory :
/Users/karthickm/Compressed/

Path to output directory :
/Users/karthickm/Decompressed   

### Running the tests

Agoda-Compression > mvn test

### Build and run the tests :

Agoda-Compression > mvn clean install

### Code Coverage report

Agoda-Compression > open target/jacoco-ut/index.html


We have used the following languages, tools and design patterns.

Language: Java [Spring Boot Framework]

Build Tool: Maven

Repository: Git

Code Coverage: Jacoco

Design Patterns : Strategy

