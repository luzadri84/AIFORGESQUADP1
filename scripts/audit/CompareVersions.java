import java.nio.file.*;
import org.apache.maven.artifact.versioning.ComparableVersion;
class CompareVersions {
 public static void main(String[] args) throws Exception {
  for (String line : Files.readAllLines(Path.of(args[0]))) {
   String[] pair=line.split("\t",-1);
   System.out.println(Integer.signum(new ComparableVersion(pair[0]).compareTo(new ComparableVersion(pair[1]))));
  }
 }
}
