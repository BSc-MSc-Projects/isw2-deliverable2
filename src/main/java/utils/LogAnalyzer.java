package main.java.utils;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;

/* This class analyzes the Git log, finding all the commit 
 * , emulating the diff command to compare two commits and to find metrics such as
 * the total number of LOC of a class, the Age etc... */
public class LogAnalyzer {
	private String url = "/home/pierciro/Scrivania/";
	private String project;
	
	public LogAnalyzer(String project) {
		this.project = project;
	}
	
	/* Retrieve the whole log for a .git repository. The commits starts form the last
	 * one to the first one, so the log is inverted (from the first one to the last one)*/	
	public List<RevCommit> findAll() {
		List<RevCommit> rcList = new ArrayList<>();
		List<RevCommit> invList = new ArrayList<>();
		
		try (var git = Git.open(new File(this.url + this.project))){
			LogCommand cmd = git.log();
			Iterable<RevCommit> commits = cmd.call();
			commits.forEach(rcList::add);
			for(int i = rcList.size()-1; i >= 0; i--) {
				RevCommit rc = rcList.get(i);
				invList.add(rc);
			}
			return invList;
		} catch (GitAPIException e) {
			Logger.getLogger("LA").log(Level.FINE, "GitAPIException\n");
		} catch (IOException e1) {
			Logger.getLogger("LA").log(Level.SEVERE, "findAll(): an error occurred while opening ."
					+ "git repo");
		}
		return invList;
	}

	
	/* Count the LOC added, modified and deleted for a class in a specified release. Return 
	 * parameter is a List of integer where:
	 * entry 0 is the sum of loc added
	 * entry 1 is the sum of loc deleted
	 * entry 2 is the maximum of loc added
	 * entry 3 is the number of commits processed for this release (needed for the NR metric)
	 * 
	 * @param commits: the list of commits for that class
	 * @param fName: the .java file name
	 * @param lowerLim: the lower bound for the time interval
	 * @param upperLim: the upper bound for the time interval
	 * @param isFirst: get the creation commit for that class*/
	public List<Integer> countLocForRelease(List<RevCommit> commits, String fName, LocalDate lowerLim, 
			LocalDate upperLim, boolean isFirst) 
			throws IOException {
		List<Integer> tempLines;
		var finalMetrics = new Integer[4];
		finalMetrics[0] = 0;
		finalMetrics[1] = 0;
		finalMetrics[2] = 0;
		finalMetrics[3] = 0;
		
		var processed = 0;
		
		LocalDate commDate;
		RevCommit old = commits.get(0);
		commDate = this.getCommitDate(old.getAuthorIdent());
		
		if(commDate.isAfter(upperLim)) {
			return Arrays.asList(finalMetrics);
		}
			
		if(isFirst) {
			tempLines = gitDiff(old, null, fName);
			finalMetrics[0] = tempLines.get(0);
			processed++;
		}
		
		RevCommit nw;
		
		for(var i = 1; i < commits.size(); i++) {
			nw = commits.get(i);
			commDate = getCommitDate(nw.getAuthorIdent());
			
			if(lowerLim == null) {
				if(commDate.isBefore(upperLim)) {
					tempLines = gitDiff(old, nw, fName);
					finalMetrics[0] += tempLines.get(0);
					finalMetrics[1] += tempLines.get(1);
					finalMetrics[2] = tempLines.get(2);
					processed++;
				}
			}
			else {
				if((commDate.isAfter(lowerLim) || commDate.isEqual(lowerLim))
					&& commDate.isBefore(upperLim)) {
					tempLines = gitDiff(old, nw, fName);
					finalMetrics[0] += tempLines.get(0);
					finalMetrics[1] += tempLines.get(1);
					finalMetrics[2] = tempLines.get(2);
					processed++;
				}
			}
			old = nw;
		}
		
		finalMetrics[3] = processed;
		return Arrays.asList(finalMetrics);
	}
	
	
	/* Compares two commit using git diff command. The output is a list of integers, in which:
	 * entry 0 is the sum of LOC inserted
	 * entry 1 is the sum of loc deleted
	 * entry 2 is the maximum of loc added
	 * 
	 * @param rev1: the first commit
	 * @param rev2: the second commit
	 * @file: the .java file */
	public List<Integer> gitDiff(RevCommit rev1, RevCommit rev2, String file) throws IOException {
		List<DiffEntry> diffList;
		var edits = new Integer[3];
		edits[0] = 0;
		edits[1] = 0;
		edits[2] = 0;
		
		try(var git = Git.open(new File(this.url + this.project));
				var formatter = new DiffFormatter(null)){
			formatter.setRepository(git.getRepository());
		
			if(rev2 != null) {
				diffList = formatter.scan(rev1.getTree(), rev2.getTree());
			}
			else {
				AbstractTreeIterator oldTreeIter = new EmptyTreeIterator();
				var reader = git.getRepository().newObjectReader();
				AbstractTreeIterator newTreeIter = new CanonicalTreeParser(null, reader, rev1.getTree());
				diffList = formatter.scan(oldTreeIter, newTreeIter);
			}
			for(DiffEntry diff: diffList){
				if(diff.toString().contains(file)) {
					formatter.setDetectRenames(true);
					var fh = formatter.toFileHeader(diff);
					var el = fh.toEditList();
					for(Edit e : el) {
						this.processEdit(e, edits);
					}
				}
				else {
					formatter.setDetectRenames(false);
				}
			}
			return Arrays.asList(edits);
		}catch(IOException e) {
			Logger.getLogger("LA").log(Level.FINE, "gitDiff(): error while opening .git repo");
		}
		return Arrays.asList(edits);
	}
	
	/* Process the information of an Edit, to check weather it is an insertion, deletition
	 * or modification
	 * 
	 * @param e: the Edit on the file
	 * @param edits: array containing the sums so far*/
	private void processEdit(Edit e, Integer[] edits) {
		var add = e.getEndB()-e.getBeginB();
		if(edits[2] == 0 || edits[2] < add)
			edits[2] = add;
		
		edits[0] += add;
		edits[1] += e.getEndA()-e.getBeginA();
	}
	
	
	// simply returns the date of a commit converted as a LocalDate
	private LocalDate getCommitDate(PersonIdent p) {
		return p.getWhen().toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}
	
	
	/* Return the list of files that are part of a specific commit
	 * 
	 * @param commit: git commit  */
	public List<ScmFile> filesInCommit(RevCommit commit){
		List<ScmFile> commitFiles = new ArrayList<>();
		try (var git = Git.open(new File(this.url + this.project));){
			var repo = git.getRepository();
			List<ScmFile> javaFiles = new ArrayList<>();
			var allFiles = JGitUtils.getFilesInCommit(repo, commit);
			for(ScmFile file: allFiles) {
				if(file.getPath().endsWith(".java"))
					javaFiles.add(file);
			}
			return javaFiles;
		} catch (IOException e) {
			Logger.getLogger("LA").log(Level.SEVERE, "Failed to open .git repository\n");
		}
		return commitFiles;
	}
}
