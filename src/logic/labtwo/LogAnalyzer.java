package logic.labtwo;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

// this class analyze the Git log, finding all the commit that contain a specific word 
//and reporting their ID's
public class LogAnalyzer {
	//private String url = "/home/pierciro/Scrivania/storm";	
	private String url = "/home/pierciro/Scrivania/avro";
	
	// get the date of the commits for a projcet
	public List<LocalDate> getCommitsTime() throws IOException {
		Git repo = Git.open(new File(this.url));
		LogCommand cmd = repo.log();
		List<LocalDate> dates = new ArrayList<>();
		
		try {
			Iterable<RevCommit> commits = cmd.call();
			for(RevCommit rev: commits) {
				Date d = new Date((long)rev.getCommitTime() * 1000);
				LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				dates.add(ld);
			}
		} catch (GitAPIException e) {
			Logger.getLogger("ANAL").log(Level.FINE, "GitAPIException\n");
		}
		return dates;
	}
	
	
	// find a commit given a Jira ticket ID, of the form "PROJ_NAME-TICK_NUMBER"
	public void findCommitByTicket(String tickId, List<String> changedList) throws IOException {
		Git git = Git.open(new File(this.url));
		LogCommand cmd = git.log();
		
		try {
			Iterable<RevCommit> commits = cmd.call();
			for(RevCommit rev: commits) {
				if(rev.getFullMessage().contains(tickId)) {
					System.out.println(rev.getName());
					//printChangedFiles(rev, git, changedList);
				}
			}
		} catch (GitAPIException e) {
			Logger.getLogger("ANAL").log(Level.FINE, "GitAPIException\n");
		}finally {
			git.close();
		}
		
	}
	
	
	// find all the commit fora specific Java file
	public RevCommit findCommitsForFile(String fileName, LocalDate start, LocalDate end) throws IOException {
		List<LocalDate> commitDates = new ArrayList<>();
		Git git = Git.open(new File(this.url));
		
		//find only the commit which affected this specific file
		LogCommand cmd = git.log().addPath(fileName);
		
		try {
			Iterable<RevCommit> commits = cmd.call();
			for(RevCommit rev: commits) {
				Date d = new Date((long)rev.getCommitTime()*1000);
				// parse the time (sec) to a LocalDate
				LocalDate commDate = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				if(commDate.isAfter(start) && commDate.isBefore(end))
					return rev;
			}
		} catch (GitAPIException e) {
			Logger.getLogger("ANAL").log(Level.FINE, "GitAPIException\n");
		}finally {
			git.close();
		}
		return null;
	}
}
