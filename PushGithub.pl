#version: 2013-10-25

#!/usr/local/bin/perl -w

use strict;
#use cwd;
#use diagnostics;

#use LWP::UserAgent;
#use LWP::Simple;
#use Win32;
#use Compress::Zlib;
#use List::Util qw(first max maxstr min minstr reduce shuffle sum);

#use Win32::GuiTest qw(FindWindowLike GetWindowText
#  SetForegroundWindow SendKeys GetForegroundWindow);

#use Clipboard; or use Win32::Clipboard
#my $CLIP = Win32::Clipboard();

#*****************************GLOBAL  VARIABLES****************************#
my $bDEBUG = 0;
my $bVerbose = 0;
my ($TRUE, $FALSE, $SUCCESS, $FAILED) = (1,0,1,0);
my $osVersion = "";

my $NEWLINE = "\r\n";

#=====================================================
my $gRootDir = "";
my $gComments = "";

my ($gSymBitbuck, $gSymGithub) = ("bitbuck", "github");

#my $domainName = "bitbucket.org";
#my $domainName = "github.com";

my %projReposMap_common = (
  "Tips_Doc"               => "git\@github.com:choclover/Tips-Doc.git|master|",

  #"SpalAdmin"              => "git\@bitbucket.org:choclover/studentpaladmin.git",
  #"MyPkgInstaller"         => "git\@bitbucket.org:choclover/mypkginstaller_froyo.git",
  #"SysPkgInstaller"        => "git\@bitbucket.org:choclover/syspkginstaller_froyo",
  );

my %projReposMap_win = (
  #4 Thumb2home Dev/Product
  #"T2H_Svr"    => "git\@bitbucket.org:thumb2home/server.git|simon_refactor1|E:/Coding/T2H/",
  "thumb2home_internal_dev" => "git\@bitbucket.org:thumb2home/server.git|thumb2home_internal_dev|E:/Coding/T2H/T2H_Svr",
  "thumb2home_product" => "git\@bitbucket.org:thumb2home/server.git|thumb2home_product|E:/Coding/T2H/T2H_Svr",

  #1
  #"StudentPalClient"       => "git\@bitbucket.org:choclover/studentpalclient.git",
  #2
  #"StudentPalClientDeamon" => "git\@bitbucket.org:choclover/studentpalclientdaemon.git",
  #3
  #"SpalSvr"                => "git\@bitbucket.org:choclover/studentpalsvr.git",

);

my %projReposMap_lnx = (
  #2. shell-scripts
  ""           => "git\@bitbucket.org:choclover/shell-scripts.git|master|/media/workspace/",

  #1
  #"T2H_Svr"                => "git\@bitbucket.org:thumb2home/server.git|simon_refactor1|/media/Coding/T2H/",

  #"SpalClient"             => "git\@bitbucket.org:choclover/studentpalclient.git",
  #"SpalClientDaemon"       => "git\@bitbucket.org:choclover/studentpalclientdaemon.git",
  #"SpalSvr"                => "git\@bitbucket.org:choclover/studentpalsvr.git",
);

if (0) {
  %projReposMap_common = (
    #"Tips_Doc"               => "git\@github.com:choclover/Tips-Doc.git",

  #Not exist "SpalAdmin"              => "git\@github.com:choclover/studentpaladmin.git",
    #"MyPkgInstaller"         => "git\@github.com:choclover/CustomPkgInstaller.git",
    #"SysPkgInstaller"        => "git\@bitbucket.org:choclover/syspkginstaller_froyo",
  );

  %projReposMap_win = (
    "StudentPalClient"       => "git\@github.com:choclover/StudentPalClient.git",
    "StudentPalClientDeamon" => "git\@github.com:choclover/StudentPalClientDaemon.git",
    "SpalSvr"                => "git\@github.com:choclover/StudentPalSvr.git",
  );

  %projReposMap_lnx = (
    "SpalClient"             => "git\@github.com:choclover/StudentPalClient.git",
    "SpalClientDaemon"       => "git\@github.com:choclover/StudentPalClientDaemon.git",
    "SpalSvr"                => "git\@github.com:choclover/StudentPalSvr.git",
  );
}

#*****************************AUXILIARY  FUNCTIONS****************************#
sub DEBUG_INFO {
  if ($bDEBUG || $bVerbose)
  {
    if (defined(@_)) {
      print "@_\n";
    } else {
      print "Not Defined!\n";
    }
  }
}
sub D {DEBUG_INFO(@_);}
sub P {print "@_\n";}

sub LOG_FILE {
  my($fileName, $bAppData, @logPara) = @_;  #bAppData -- append date to file or overwrite file
  #DEBUG_INFO($fileName, $bAppData);
  $fileName =~ s!\\!/!ig;
  my @pathAry = split('/', $fileName);
  my $tmpPath = "";
  for (my $i=0; $i<scalar(@pathAry)-1; $i++) {
      $tmpPath .= $pathAry[$i] . '/';   #D($tmpPath);
      mkdir($tmpPath, 0111) if (! -d $tmpPath);
  }
  if ($bAppData) {$fileName = " >> " . $fileName;  #append data
  } else         {$fileName = " > "  . $fileName;}

  open(tmpLogFile, $fileName) || die "Cannot open log file: $fileName!\n";
  foreach (@logPara) {
    my ($str0D, $str0A) = ('\r', '\n');
    s/$str0D//ig;  #remove all '\r' chars
    print tmpLogFile "$_\n";
  }
  close(tmpLogFile);
}

sub trim($) {
    my $string = shift;
    $string =~ s/^\s+//;  $string =~ s/\s+$//;
    return $string;
}

sub isEmptyStr {
    my ($result, $str) = (0, @_);
    $result = 1 if (!defined($str) || $str eq "" || $str=~m/^\s+$/ig);
    return $result;
}

sub parse_args {
  P(@_);
  for (my $i=0; $i<scalar(@_); $i++) {
    if ($_[$i] eq "-debug") {
      $bDEBUG = $TRUE;   P("** Running in DEBUG mode !**");
    } elsif ($_[$i] eq "-v") {
      $bVerbose = $TRUE; P("** Running in VERBOSE mode !**");

    } elsif ($_[$i] eq "-m") {
      if (defined $_[$i+1]) {
        $gComments = $_[$i+1];
        $i++;
      }
    }
  }
  if (defined $^O) {$osVersion =  $^O;} else {$osVersion = "win32"; }
  P("osVersion is: $osVersion");
}

sub backupFile {
  my ($oldFName) = @_;
  if (-e $oldFName) {
    my ($suffix,$newFName) = (0, "");
    do {
      $suffix++;
      if (rindex($oldFName, '.') > 0) {
        $newFName = sprintf("%s_bak%02d%s", substr($oldFName, 0, rindex($oldFName, '.')),
                            $suffix, substr($oldFName, rindex($oldFName, '.')) );
      } else {
        $newFName .=  sprintf("_bak%02d", $suffix);
      }
    } while (-e $newFName);
    rename($oldFName, $newFName);
  }
}

sub isWindowsArch {
  return ($osVersion =~ /win32/i);
}
sub isCygwinArch {
  return ($osVersion =~ /msys|cygwin/i);
}

sub runSysCmd {
  my ($cmdStr) = @_;
  D("\nCommand is:\n $cmdStr");
  my $result = 0;
  $result = system($cmdStr) if (!$bDEBUG);
  P("Run command returns error") if ($result != 0);
  return $result;
}

sub getDate {
  my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time());
  return sprintf("%04d-%02d-%02d %02d:%02d", $year+1900, $mon+1, $mday, $hour, $min);
}
###############################################################################
sub main {
  my %projReposMap;
  if (isWindowsArch()) {
    P("This is Windows arch!");
    $gRootDir = "e:/Coding/Android/";
    %projReposMap = %projReposMap_win;

  } elsif (isCygwinArch()) {
    P("This is Cygwin arch!");
    $gRootDir = "/e/Coding/Android/";
    %projReposMap = %projReposMap_win;

  } else {
    P("This is Linux arch!");
    $gRootDir = "/media/Coding/And/";
    %projReposMap = %projReposMap_lnx;
  }

  %projReposMap = (%projReposMap, %projReposMap_common);

  D("ProjRepoMap is:", sort keys %projReposMap);

  while (1) {
    print_usage();
    my $option = <STDIN>;   chomp $option;
    D("option is $option") ;
    next if (!defined $option);

    if ('1' eq $option) {
      pull_repos($gSymBitbuck, \%projReposMap);
    } elsif ('2' == $option) {
      push_repos($gSymBitbuck, \%projReposMap, $TRUE);

    } elsif ('3' eq $option) {
      status_repos(            \%projReposMap);
    } elsif ('4' == $option) {
      push_repos($gSymBitbuck, \%projReposMap, $FALSE);

    } elsif ('0' == $option) {
      P("Exiting...\n");
      exit 1;

    } else {
    }
  }
}

sub pull_repos {
  my ($repoSym, $refProjReposMap) = @_;
  D("ProjReposMap is: ", %$refProjReposMap);

  foreach my $dire (sort keys %$refProjReposMap) {
    my $reposInfo = $$refProjReposMap{$dire};
    my ($gitUrl, $gitBranch, $rootDir) = split('\|', $reposInfo);
    D($gitUrl, $gitBranch, $rootDir);

    my $path = "$gRootDir/$dire";
    if ($FALSE == isEmptyStr($rootDir)) {
      $path = "$rootDir/$dire";
    }
    P("\n@@ cd $path\n");

    if (! defined $gitBranch) {
      $gitBranch = "master";
    }

    my $cdDir = "";
    my $cmdStr = "";

    if (not -e $path) {
      $cdDir = "cd $path/..; ";
      $cmdStr = $cdDir . "git clone $gitUrl $dire;  ";
      runSysCmd($cmdStr);

    } else {
      $cdDir = "cd $path; ";
      $cmdStr = "";

      #$cmdStr = $cdDir . "git add -A; git commit -a -m '". getComment($gitBranch) ." commit'; ";
      #runSysCmd($cmdStr);

      #$cmdStr = "git pull $repoSym $gitBranch; ";
      $cmdStr = $cdDir . "git pull $gitUrl $gitBranch; ";
      $cmdStr = $cdDir . "git pull $gitUrl $gitBranch --tag; ";

      my $cnt = 0;
      while (runSysCmd($cmdStr) != 0  && $cnt<10) {
        sleep(3);
        $cnt++;
      }
    }
  }
}

sub push_repos {
  my ($repoSym, $refProjReposMap, $bPushRemote) = @_;
  D("ProjReposMap is: ", %$refProjReposMap);

  foreach my $dire (sort keys %$refProjReposMap) {
    my $reposInfo = $$refProjReposMap{$dire};  D($reposInfo);
    my ($gitUrl, $gitBranch, $rootDir) = split('\|', $reposInfo);
    D("GitUrl: $gitUrl; \nGitBranch: $gitBranch; \nRootDir: $rootDir");

    my $path;
    if ($FALSE == isEmptyStr($rootDir)) {
      $path = "$rootDir/$dire";
    } else {
      $path = "$gRootDir/$dire";
    }

    if (! -d $path) {
      P("Path $path NOT existing! **");
      next;
    }
    P("\n@@ cd $path");

    if (isEmptyStr($gitBranch)) {
      $gitBranch = "master";
    }

    my $cdDir = "cd $path; ";
    my $cmdStr = "";

    $cmdStr = $cdDir . "git add -A; git commit -a -m '"
              .getComment($gitBranch). " on branch($gitBranch)'; ";
    if ((0!=runSysCmd($cmdStr) && $FALSE==$bPushRemote)  #commit fails
        || $FALSE==$bPushRemote) {
      next;
    }

    #$cmdStr = "git push github $gitBranch; ";
    if ($bPushRemote) {
      #$cmdStr = $cdDir . "git push $repoSym $gitBranch; ";
      $cmdStr = $cdDir . "git push $gitUrl $gitBranch; ";
      $cmdStr = $cdDir . "git push $gitUrl $gitBranch --tag; ";
    }

    my $cnt = 1;
    while (runSysCmd($cmdStr) != 0 && $cnt<=5) {
      P("## Trying No. $cnt time, " .(5-$cnt). " times remain.");
      sleep(3);
      $cnt++;
    }
  }
}

sub status_repos {
  my ($refProjReposMap) = @_;
  D("ProjReposMap is: ", %$refProjReposMap);

  foreach my $dire (sort keys %$refProjReposMap) {
    my $reposInfo = $$refProjReposMap{$dire};  D($reposInfo);
    my ($gitUrl, $gitBranch, $rootDir) = split('\|', $reposInfo);
    D("GitUrl: $gitUrl; \nGitBranch: $gitBranch; \nRootDir: $rootDir");

    my $path;
    if ($FALSE == isEmptyStr($rootDir)) {
      $path = "$rootDir/$dire";
    } else {
      $path = "$gRootDir/$dire";
    }

    if (! -d $path) {
      P("Path $path NOT existing! **");
      next;
    }
    P("\n@@ cd $path");

    my $cdDir = "cd $path; ";
    my $cmdStr = "";

    $cmdStr = $cdDir . "git status; git remote -v ";
    runSysCmd($cmdStr);
  }
}

sub getComment {
  my ($branchName) = @_;
  if (isEmptyStr($gComments)) {
    return getDate() . " commit ";
  } else {
    return $gComments;
  }
}

sub print_usage {
  print"\n";
  printf("*** Function SELECTOR ***\n");
  printf("* 1. Pull From Repos    *\n");
  printf("* 2. Push To   Repos    *\n");
  printf("* 3. Status of Repos    *\n");
  printf("* 4. Commit To Repos    *\n");
  #printf("* 4. Push GitHub       *\n");
  printf("* 0. Exit               *\n");
  printf("*************************\n");

  printf("\nChoose An Option: ");
}
###############################################################################

parse_args(@ARGV);

if (1) {
  main();
} else {
  Test();
}
