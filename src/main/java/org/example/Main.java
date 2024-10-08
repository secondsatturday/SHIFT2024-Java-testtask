package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Main {
    static List<Path> inputPaths = new ArrayList<>();
    static String prefix = "";
    static String outputPath = "./";
    static boolean defaultToAppend = false;    // Заданное значение - добавлять в конец файла / перезаписывать его
    static int showStatistics = 0;  // 0 - hide stat, 1 - short stat, 2 - full stat
    // Если нужно добавить строку в конец файла, а не перезаписывать файл с нуля, то значение для имени файла будет true
    static Map<String,Boolean> filesToAppend = new HashMap<>();

    static Integer numInt = 0;
    static Integer minInt = 0;
    static Integer maxInt = 0;
    static Integer sumInt = 0;

    static Integer numFloat = 0;
    static Float minFloat = 0F;
    static Float maxFloat = 0F;
    static Float sumFloat = 0F;

    static Integer numString = 0;
    static Integer minLenString = 0;
    static Integer maxLenString = 0;

    public static void handleArgs(List<String> args) {

        List<String> receivedOptions = new ArrayList<>();
        boolean repeatedOptionError = false;
        boolean pathReceived = false;
        boolean pathBeforeOptionError = false;
        boolean conflictingOptionsError = false;

        while(!args.isEmpty()) {
            String curArg = args.remove(0);

            if(receivedOptions.contains(curArg)) {
                repeatedOptionError = true;
            }
            if(pathReceived) {
                pathBeforeOptionError = true;
            }

            switch(curArg) {
                /* Попытка обработать очередной аргумент, как опцию. */
                case "-o" -> {
                    if(args.isEmpty()) {
                        System.err.println("Input error. An option argument was excepted.");
                        System.exit(-1);
                    }
                    outputPath = args.remove(0);
                }
                case "-p" -> {
                    if(args.isEmpty()) {
                        System.err.println("Input error. An option argument was excepted.");
                        System.exit(-1);
                    }
                    prefix = args.remove(0);
                }
                case "-a" -> defaultToAppend = true;
                case "-s" -> {
                    if(receivedOptions.contains("-f")) {
                        conflictingOptionsError = true;
                    }
                    showStatistics = 1;
                }
                case "-f" -> {
                    if(receivedOptions.contains("-s")) {
                        conflictingOptionsError = true;
                    }
                    showStatistics = 2;
                }
                default -> {
                    /* Обработка пути к файлу. Если очередной аргумент не
                    опция => это путь к входному файлу. */
                    pathBeforeOptionError = false;
                    pathReceived = true;
                    /* Обработка повторно встретившегося пути к файлу. Просто
                    не добавляет путь в лист файлов ввода повторно. */
                    if(repeatedOptionError) {
                        repeatedOptionError = false;
                    } else {
                        inputPaths.add(Path.of(curArg));
                    }
                }
            }
            if(pathBeforeOptionError) {
                System.err.println("Input error. Input file path excepted, option received.");
                System.exit(-1);
            }
            if(repeatedOptionError) {
                System.err.println("Input error. Re-entering the same option.");
                System.exit(-1);
            }
            if(conflictingOptionsError) {
                System.err.println("Input error. Conflicting options received.");
                System.exit(-1);
            }
            receivedOptions.add(curArg);
        }
    }

    public static void doOutput() {
        /* Сначала считываем строки из всех входных файлов, затем производим
        фильтрацию. Фильтраия происходит при вызове методов parseInt(), parseFloat()
        и обработке их исключений. */

        List<String> inputLines = new ArrayList<>();

        while(!inputPaths.isEmpty()) {
            Path curPath = inputPaths.remove(0);

            if(Files.exists(curPath)) {
                try {
                    inputLines.addAll(Files.readAllLines(curPath));
                } catch(IOException e) {
                    System.err.println("File error. IOException catched.");
                    System.exit(-1);
                }
            } else {
                System.err.println("File error. Input file doesn't exists.");
                System.exit(-1);
            }
        }

        /* Фильтрация полученных строк и сбор статистики */
        List<String> classReceived = new ArrayList<>();
        ListIterator<String> itr = inputLines.listIterator();
        while(itr.hasNext()) {
            Object element = itr.next();
            try {
                Integer a = Integer.parseInt(element.toString());
                print(a);
                if(!classReceived.contains(a.getClass().getSimpleName())) {
                    classReceived.add(a.getClass().getSimpleName());
                    numInt = 1;
                    minInt = a;
                    maxInt = a;
                    sumInt = a;
                } else {
                    numInt += 1;
                    if(a < minInt) minInt = a;
                    if(a > maxInt) maxInt = a;
                    sumInt += a;
                }
            } catch(NumberFormatException e1) {
                try {
                    Float a = Float.parseFloat(element.toString());
                    print(a);
                    if(!classReceived.contains(a.getClass().getSimpleName())) {
                        classReceived.add(a.getClass().getSimpleName());
                        numFloat = 1;
                        minFloat = a;
                        maxFloat = a;
                        sumFloat = a;
                    } else {
                        numFloat += 1;
                        if(a < minFloat) minFloat = a;
                        if(a > maxFloat) maxFloat = a;
                        sumFloat += a;
                    }
                } catch(NumberFormatException e2) {
                    String a = element.toString();
                    print(a);
                    if(!classReceived.contains(a.getClass().getSimpleName())) {
                        classReceived.add(a.getClass().getSimpleName());
                        numString = 1;
                        minLenString = a.length();
                        maxLenString = a.length();
                    } else {
                        numString += 1;
                        if(a.length() < minLenString) minLenString = a.length();
                        if(a.length() > maxLenString) maxLenString = a.length();
                    }
                }
            }
        }

        /* Вывод статистики */
        if(showStatistics == 1) {
            /* Вывод краткой статистики */
            System.out.println("Short statistics.");
            System.out.println("The number of elements of each type.");
            if(classReceived.contains("Integer")) System.out.println("integers: " + numInt);
            if(classReceived.contains("Float")) System.out.println("floats: " + numFloat);
            if(classReceived.contains("String")) System.out.println("strings: " + numString);
        } else if(showStatistics == 2) {
            /* Вывод полной статистики */
            System.out.println("Full statistics.");
            if(classReceived.contains("Integer")) {
                System.out.println("Integer elements.");
                System.out.println("Number: " + numInt);
                System.out.println("Min: " + minInt);
                System.out.println("Max: " + maxInt);
                System.out.println("Sum: " + sumInt);
                System.out.println("Average: " + (sumInt / numInt) + "\n");
            }

            if(classReceived.contains("Float")) {
                System.out.println("Float elements.");
                System.out.println("Number: " + numFloat);
                System.out.println("Min: " + minFloat);
                System.out.println("Max: " + maxFloat);
                System.out.println("Sum: " + sumFloat);
                System.out.println("Average: " + (sumFloat / numFloat) + "\n");
            }

            if(classReceived.contains("String")) {
                System.out.println("String elements.");
                System.out.println("Number: " + numString);
                System.out.println("Min length: " + minLenString);
                System.out.println("Max length: " + maxLenString);
            }
        }

    }

    public static <T> void print(T line) {
        /* Собирается полный путь до файла вывода из пути и префикса, переданных
        в аргументы программы, а также класса T; происходит попытка записи строки
        в файл. */
        try {
            /* Сборка пути к файлу вывода */
            Path fullOutputPath = Path.of(outputPath, prefix + line.getClass().getSimpleName().toLowerCase() + "s.txt");

            /* Заполнение словаря значениями по умолчанию. Значение из словаря,
            спеифичное для каждого файла вывода, позволяет определить, необходимо
            записывать новую строку в конец уже существующего файла (т.е. исполь-
            зовать опцию APPEND) или начинать запись с начала.
            */
            if(!filesToAppend.containsKey(fullOutputPath.toString())) {
                filesToAppend.put(fullOutputPath.toString(), defaultToAppend);
            }

            /* Попытка записи строки в файл */
            String str = line.toString() + "\n";
            if(filesToAppend.get(fullOutputPath.toString())) {
                Files.writeString(fullOutputPath, str, StandardOpenOption.APPEND);
            } else {
                /* Если произошла запись в файл, то последующие строки должны
                записываться в конец данного файла (опция APPEND). Делаем
                соответствующее изменение в словаре для данного файла вывода. */
                Files.writeString(fullOutputPath, str);
                filesToAppend.put(fullOutputPath.toString(), true);
            }
        } catch(IOException e) {
            System.err.println("File error. IOException catched.");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        handleArgs(new ArrayList<String>(Arrays.asList(args)));
        doOutput();
    }
}