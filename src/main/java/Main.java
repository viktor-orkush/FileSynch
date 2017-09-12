import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;

import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class ComparePathStructure {
    private Path sour;
    private Path dest;
    private LinkedList<Path> sourLL;
    private LinkedList<Path> destLL;

    ComparePathStructure(Path sour, Path dest) {
        this.sour = sour;
        this.dest = dest;
        if (Files.notExists(dest)) {
            try {
                Files.createDirectories(dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Compare() {
        try {
            MyFileVisitor fvFoSour = new MyFileVisitor();
            MyFileVisitor fvFoDest = new MyFileVisitor();

            walkFileTree(sour, fvFoSour);
            walkFileTree(dest, fvFoDest);

            sourLL = fvFoSour.getAllPaths();
            destLL = fvFoDest.getAllPaths();

            comparePathSourAndDest(sourLL, destLL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void comparePathSourAndDest(LinkedList<Path> sourLL, LinkedList<Path> destLL) {
        //проверяем папку/файл если такого нет, то создаем и удаляем его с  destLL коллекции
        for (Path sourFromList : sourLL) {
            try {
                File f = new File(sourFromList.toString());
                if (!f.exists() && Files.isDirectory(sourLL.getFirst())) {
                    createDirection(destLL, sourFromList);
                } else {
                    copyFile(destLL, sourFromList);
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        //удаляем все оставшиеся папки/файлы
        for (int i = destLL.size() - 1 ; i >= 0 ; i--) {
            try {
                Files.deleteIfExists(destLL.get(i));

                System.out.println("delete  " + destLL.get(i));
            } catch (IOException e) {
                System.out.println("директория не пустая" + e.getMessage());
            }
        }
    }

    private void createDirection(LinkedList<Path> destLL, Path sourFromList) throws IOException {
        Path subdirectoryOfSour = sour.relativize(sourFromList);
        if (subdirectoryOfSour != null) {
            Path destPathToSubdirectory = Paths.get(dest.toString(), subdirectoryOfSour.toString());
            if (!Files.exists(destPathToSubdirectory)) {
                System.out.println("createDirectories: " + destPathToSubdirectory);
                Files.createDirectories(destPathToSubdirectory);
            } else {
                destLL.remove(destPathToSubdirectory);
            }
        }
    }

    private void copyFile(LinkedList<Path> destLL, Path sourFromList) throws IOException {
        Path pathToFile = sour.relativize(sourFromList);
        if (pathToFile != null) {
            Path destPathToFile = Paths.get(dest.toString(), pathToFile.toString());
            if (!Files.exists(destPathToFile) || Files.size(destPathToFile) != Files.size(sourFromList)) {
                System.out.println("copy file  " + destPathToFile);
                Files.copy(sourFromList, destPathToFile, REPLACE_EXISTING);
            }
            destLL.remove(destPathToFile);
        }
    }

    class MyFileVisitor implements FileVisitor<Path> {
        private LinkedList<Path> allPaths = new LinkedList<>();

        public LinkedList<Path> getAllPaths() {
            return allPaths;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            allPaths.add(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            allPaths.add(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
        //D:\java\pathSource D:\java\pathDest\path2
        if(args.length == 2){
            Path sourPath = Paths.get(args[0]);
            Path destPath = Paths.get(args[1]);
            ComparePathStructure comparePathStructure = new ComparePathStructure(sourPath, destPath);
            comparePathStructure.Compare();
        }
        else{
            throw new IllegalArgumentException("Введено не правельное количество аргументов!");
        }

    }
}
