# Agoda Take Home Challenge

Write a program that compresses files and folders into a set of compressed files such that of each compressed file doesn’t exceed a maximum size. The same program can be used for decompressing the files that it has generated earlier. The output of the decompression should be identical to the original input of the compression process.

# Key Challenges and Constraints: 

 1. Input files can be very large. [More than JVM size].
 2. Compressed files generated can also be very large. [More than MaxCompressedSizeThresold]
 3. Compression output should generate less files.
 4. Support different compression algorithms.
 5. Make parallel calls while compression and decompression, as much as possible.
 
# Our Approach: 

As mentioned in the problem, we use zip for compression algorithm [JDK Implementation]. However, our algorithm supports other compression algorithms as well and we have used strategy design pattern for the same. [Point 4]

# Input Validation:

Our Input Requests for compression and decompression is validated with the validator and throws exceptions, if necessary, such as Invalid Input Directory, Input Directory with no read permissions, Input Directory does not exist, Input directory is not valid etc. We use long datatype for MaxCompressedSizeThresold input. [expressed in MB]

# Compression:

[Points 1, 2, 3]: The key problem in this challenge is, how do we handle large files and generate less compressed files as possible.

To handle above issues, we perform 2 levels of compression : 

In the first level, if the input file is more than MaxCompressedSizeThresold, we split the given file into chunks of size MaxCompressedSizeThresold and zip it. Otherwise, we simply compress the input file.

Eg: File1  = 10 MB, File2 = 2 MB, MaxCompressedSizeThresold = 4 MB.

Input files after splitting : File1.part_0.level1 (4MB), File1.part_1.level1 (4MB), File1.part_2.level1 (2MB), File2.part_0.level1 (2MB)

Level 1 output : File1.part_0.level1.zip, File1.part_1.level1.zip, File1.part_2.level1.zip, File2.part_0.level1.zip

The sizes of above compressed files vary depends on the file content, media type and the compression.

In the second level, in order to generate the minimum files, we perform the second level compression as follows.

We group zips of selected files that sums upto MaxCompressedSizeThresold and zip it again. We repeat the above process and create level 2 zips.

For Eg :  File1 (14 MB), File2(2 MB), File3 (2 MB), File4(2 MB), File5 (2 MB), File6 (2 MB), File7 (2 MB), File8(2 MB), File9 (2 MB), File10 (1 MB)

MaxCompressedSizeThresold = 4 MB. For simplicity, assume the level 1 output produces following zip with the file sizes mentioned.

Level 1 output :  File1.part_0.level1.zip (4 MB), File1.part_1.level1.zip (4MB) , File1.part_2.level1.zip(4MB),
File1.part_3.level1.zip(2MB), File2.part_0.level1.zip (1MB), File3.part_0.level1.zip (1MB), File4.part_0.level1.zip (1MB), File5.part_0.level1.zip (1MB), File6.part_0.level1.zip (1MB), File7.part_0.level1.zip (1MB), File8.part_0.level1.zip (1MB), File9.part_0.level1.zip (1MB), File10.part_0.level1.zip (1MB)

Level 2 output: Level_2_part_0.level2.zip (4MB) [Grouping files 2-5], Level_2_part_1.level2.zip (4MB) [Grouping files 6-9], Level_2_part_2.level2.zip (3MB) [Grouping files 10 and 4th chunk of File 1], File1.part_0.level1.zip (4 MB), File1.part_1.level1.zip (4MB) , File1.part_2.level1.zip(4MB)


We have generated only 6 files as shown in level 2 output, instead of 13 in level 1 output. 

We can realize the benefit of level 2 compression with more number of input files that consists of several large and small files.

To perform level 2 compressions, we sort the output of level 1 files based on file sizes and then group the chunks.
The files can present in different directories and at different depth, however our algorithm chooses files wisely and perform level 2 compression and generate the minimum number of files.

# Decompression:

Decompression follows the reverse process of compression to get the original input files.

Here, we first perform level 2 decompressions if any, followed by level 1 decompressions. Level 2 zips may not have been 

generated, in some cases. Say the level 1 zip sizes are already same as MaxCompressedSizeThresold, then,

level 2 zips would not have been generated and we perform only level 1 decompressions.

After level 1 decompressions, we merge the chunks that were split before by our compression algorithm and 

generates the required output files.

# Concurrency:

[Point 5]. We use Java Executor services to make parallel calls during compression and decompression. Thread Pool size is set to 20, for our application. We ensures synchronization and wait for threads to complete when necessary. Eg., Level 2 compressions requires output of level 1 compression zips. 

In the compression input request, maximum compressed size per file is given as input. It can be greater than JVM size. 

Hence, to maximize threads for parallel execution satisfying JVM constraints, we calculate MaxCompressedSizeThreshold as follows:

MaxCompressedSizeThresold = Minimum of { MaxCompressedSizeThresold Input, (JVM Max. allowed memory/Thread_Pool_Size) }.

Eg 1 : MaxCompressedSizeThresold = 100 MB, JVM Max. allowed memory = 3000 MB, Thread_Pool_Size = 20

MaxCompressedSizeThresold = Min (100, 150) = 100 MB

Eg 2 : MaxCompressedSizeThresold = 200 MB, JVM Max. allowed memory = 3000 MB, Thread_Pool_Size = 20

MaxCompressedSizeThresold = Min (200, 150) = 150 MB.

Here, our file chunk size can be upto 150 MB and all 20 threads can run tasks concurrently as it does not exceed JVM Max memory (20*150MB = 3000MB)

## Getting Started

### Git clone:

Desktop > git clone https://github.com/mkarthick19/Agoda-Compression.git

### Running the application 

cd Agoda-Compression/

### Build and run the tests :

Agoda-Compression > mvn clean install

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

We can see the generated compressed files in the output directory /Users/karthickm/Compressed/

# Sample Decompression 

Agoda-Compression > mvn spring-boot:run

Please enter 1 for Compression and 2 for Decompression :
2

Please enter the following:

Path to input directory :
/Users/karthickm/Compressed/

Path to output directory :
/Users/karthickm/Decompressed   

We can see the generated output files in the output directory /Users/karthickm/Decompressed

### Running the tests

Agoda-Compression > mvn test

### Code Coverage report

Agoda-Compression > open target/jacoco-ut/index.html

### Languages and tools Used:

We have used the following languages, tools and design patterns.

Language: Java [Spring Boot Framework]

Build Tool: Maven

Repository: Git

Code Coverage: Jacoco

Design Patterns : Strategy

