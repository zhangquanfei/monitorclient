package monitortest;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class WatchRecursiveRafaelNadal {

    private WatchService watchService;
    private final Map<WatchKey, Path> directories = new HashMap<>();

    private void registerPath(Path path) throws IOException {
        //register the received path
        WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

        //store the key and path
        directories.put(key, path);
    }
    private void registerTree(Path start) throws IOException {

        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {

                String[] ss = {"/home/quanfei/app","/home/quanfei/rvm","/home/quanfei/.rvm"};


                boolean b = false;

                for (int i = 0; i < ss.length; i++) {
                    if (dir.toString().equals(ss[i])){
                        b = true;
                    }
                    if (dir.toString().length() > ss[i].length()) {
                        if (dir.toString().contains(ss[i])){
                            b = true;
                        }
                    }
                }
                if (b == false){
                    System.out.println("register:"+dir);
                    registerPath(dir);

                }


//                String[] ss = {"/home/quanfei/app","/home/quanfei/rvm"};
//
//
//                for(int i =0;i<ss.length;i++){
//                    if (dir.toString().equals(ss[i])){
//                        //过滤
//                    }else {
//
//                        if (dir.toString().length() > ss[i].length()) {
//
//                            char[] excludes = ss[i].toCharArray();
//                            char[] starts = dir.toString().toCharArray();
//                            Boolean  bool = false;
//                            for (int j = 0; j < excludes.length; j++) {
//                                if (starts[j] != excludes[j]) {
//                                    bool = true;
//                                }
//                            }
//                            if (bool == true) {
//                                System.out.println("register:"+dir);
//                                registerPath(dir);
//                                bool = false;
//                            }
//                        }else {
//                            System.out.println("register:"+dir);
//                            registerPath(dir);
//                        }
//                    }
//
//
//                }



                //System.out.println("==="+dir.toString());
//                if(!Arrays.asList(ss).contains(dir.toString()))
//                {
//                System.out.println("Registering:" + dir);
//                    WatchRecursiveRafaelNadal.sss = "Registering:" + dir;
//                registerPath(dir,events);
//                }


                return FileVisitResult.CONTINUE;
            }
        });

    }

    public void watchRNDir(Path start) throws IOException, InterruptedException {

        watchService = FileSystems.getDefault().newWatchService();

        registerTree(start);

        //start an infinite loop
        while (true) {

            //retrieve and remove the next watch key
            final WatchKey key = watchService.take();

            //get list of events for the watch key
            for (WatchEvent<?> watchEvent : key.pollEvents()) {

                //get the kind of event (create, modify, delete)
                final Kind<?> kind = watchEvent.kind();

                //get the filename for the event
                final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                final Path filename = watchEventPath.context();

                //handle OVERFLOW event
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                //handle CREATE event
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    final Path directory_path = directories.get(key);
                    final Path child = directory_path.resolve(filename);

                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        registerTree(child);
                    }
                }
                //print it out
                System.out.println(kind + " -> " + filename);
            }

            //reset the key
            boolean valid = key.reset();

            //remove the key if it is not valid
            if (!valid) {
                directories.remove(key);

                //there are no more keys registered
                if (directories.isEmpty()) {
                    break;
                }
            }
        }
        watchService.close();
    }




    public static void main(String[] args) {

        final Path path = Paths.get("/home/quanfei");
        WatchRecursiveRafaelNadal watch = new WatchRecursiveRafaelNadal();

        try {
            watch.watchRNDir(path);
        } catch (IOException | InterruptedException ex) {
            System.err.println(ex);
        }
    }
}