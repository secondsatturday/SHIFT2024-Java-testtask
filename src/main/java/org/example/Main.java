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

        ListIterator itr = inputPaths.listIterator();
        while(itr.hasNext()) {
            Object el = itr.next();
            System.out.println(el);
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

        /* Фильтрация полученных строк */
        ListIterator<String> itr = inputLines.listIterator();
        while(itr.hasNext()) {
            Object element = itr.next();

            try {
                print(Integer.parseInt(element.toString()));
            } catch(NumberFormatException e1) {
                try {
                    print(Float.parseFloat(element.toString()));
                } catch(NumberFormatException e2) {
                    print(element.toString());
                }
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