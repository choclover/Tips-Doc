#version: 2011-11-17

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
my ($TRUE, $FALSE, $SUCCESS, $FAILED) = (1,0,1,0);
my $osVersion = "";

my $NEWLINE = "\r\n";

#=====================================================
my $gRootDir = "";

my ($gSymBitbuck, $gSymGithub) = ("bitbuck", "github");

my %projReposMap_win = (
  #1
  "StudentPalClient"       => "git\@bitbucket.org:choclover/studentpalclient.git",        
  #2
  "StudentPalClientDeamon" => "git\@bitbucket.org:choclover/studentpalclientdaemon.git",
  #3
  "SpalSvr"                => "git\@bitbucket.org:choclover/studentpalsvr.git",
  #4
  "MyPkgInstaller"       => "git\@bitbucket.org:choclover/mypkginstaller_froyo.git",
  #"MyPkgInstaller"       => "git\@github.com:choclover/CustomPkgInstaller.git",
  #5
  "Tips_Doc"               => "git\@github.com:choclover/Tips-Doc.git",
);

my %projReposMap_lnx = (
  "SpalClient"             => "git\@bitbucket.org:choclover/studentpalclient.git",
  "SpalClientDaemon"       => "git\@bitbucket.org:choclover/studentpalclientdaemon.git",
  "SpalSvr"                => "git\@bitbucket.org:choclover/studentpalsvr.git",
  "MyPkgInstaller"         => "git\@bitbucket.org:choclover/mypkginstaller_froyo.git",
  #"MyPkgInstaller"         => "git\@github.com:choclover/CustomPkgInstaller.git",
  "Tips_Doc"               => "git\@github.com:choclover/Tips-Doc.git",
);

#*****************************AUXILIARY  FUNCTIONS****************************#
sub DEBUG_INFO {
  return if (!$bDEBUG);
  if (defined(@_)) {
    print "@_\n";
  } else {
    print "Not Defined!\n";
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
      $bDEBUG = $TRUE;   #D("bDEBUG is set to: $bDEBUG");
    } else {

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
  D("\nCommand is: $cmdStr");
  my $result = 0;
  $result = system($cmdStr) if (!$bDEBUG);
  P("Run command returns error") if ($result != 0);
  return $result;
}

sub getDate {
  my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time());
  return sprintf("%04d-%02d-%02d", $year+1900, $mon+1, $mday);
}
###############################################################################
sub main {
  my $refProjRepoMap;
  if (isWindowsArch()) {
    D("This is Windows arch!");
    $gRootDir = "E:/Coding/Android/";
    $refProjRepoMap = \%projReposMap_win;

  } elsif (isCygwinArch()) {
    D("This is Cygwin arch!");
    $gRootDir = "E:/Coding/Android/";
    $refProjRepoMap = \%projReposMap_win;

  } else {
    D("This is Linux arch!");
    $gRootDir = "/media/Coding/And/";
    $refProjRepoMap = \%projReposMap_lnx;
  }
  D("ProjRepoMap is:", sort keys %$refProjRepoMap);

  while (1) {
    print_usage();
    my $option = <STDIN>;   chomp $option;
    D("option is $option") ;
    next if (!defined $option);

    if ('1' eq $option) {
      pull_repos($gSymBitbuck, $refProjRepoMap);
    } elsif ('2' == $option) {
      push_repos($gSymBitbuck, $refProjRepoMap);

    } elsif ('3' eq $option) {
      status_repos($refProjRepoMap);
    } elsif ('4' == $option) {
      push_repos($gSymGithub, $refProjRepoMap);

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
  	my $gitUrl = $$refProjReposMap{$dire};
  	my $path = "$gRootDir/$dire";  P("\n$path\n");

    my $cdDir = "cd $path; ";
    my $cmdStr = "";
    
    #$cmdStr = $cdDir . "git add -A; git commit -a -m '". getDate() ." commit'; ";
    #runSysCmd($cmdStr);
    
    #$cmdStr = "git pull $repoSym master; ";
    $cmdStr = $cdDir . "git pull $gitUrl master; ";

    my $cnt = 0;
    while (runSysCmd($cmdStr) != 0  && $cnt<10) {
      sleep(3);
      $cnt++;
    }
  }
}

sub push_repos {
  my ($repos, $refProjReposMap) = @_; 
  D("ProjReposMap is: ", %$refProjReposMap);
  
  foreach my $dire (sort keys %$refProjReposMap) {
  	my $gitUrl = $$refProjReposMap{$dire};
  	my $path = "$gRootDir/$dire";  P("\n$path\n");

    my $cdDir = "cd $path; ";
    my $cmdStr = "";
    
    $cmdStr = $cdDir . "git add -A; git commit -a -m '". getDate() ." commit'; ";
    runSysCmd($cmdStr);
    
    #$cmdStr = "git push github master; ";
    $cmdStr = $cdDir . "git push $gitUrl master; ";

    my $cnt = 0;
    while (runSysCmd($cmdStr) != 0 && $cnt<10) {
      sleep(3);
      $cnt++;
    }
  }
}

sub status_repos {
  my ($refProjReposMap) = @_; 
  D("ProjReposMap is: ", %$refProjReposMap);
  
  foreach my $dire (sort keys %$refProjReposMap) {
  	my $gitUrl = $$refProjReposMap{$dire};
  	my $path = "$gRootDir/$dire";  P("\n$path\n");

    my $cdDir = "cd $path; ";
    my $cmdStr = "";
    
    $cmdStr = $cdDir . "git status ";
    runSysCmd($cmdStr);
  }
}


sub print_usage {
  print"\n";
  printf("*** Function SELECTOR ***\n");
  printf("* 1. Pull From Repos    *\n");
  printf("* 2. Push To   Repos    *\n");
  printf("* 3. Status of Repos    *\n");
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
