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
my $gRootDir = "";

my @projList_win = (
  "StudentPalClient", "StudentPalClientDeamon", "SpalSvr",
  "PackageInstaller", "Tips_Doc",
);

my @projList_lnx = (
  "SpalClient", "SpalClientDaemon", "SpalSvr",
  "CustomPkgInstaller", "Tips_Doc",
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
  if (defined $^O) {$osVersion =  $^O;} else {$osVersion = "win32"; }  P("osVersion is: $osVersion");
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
  return ($osVersion =~ /msys/i);
}

sub runSysCmd {
  my ($cmdStr) = @_;
  D("\n\nCommand is: $cmdStr");
  my $result = system($cmdStr);
  P("Run command returns error") if ($result != 0);
  return $result;
}

###############################################################################
sub main {
  my $refProjList;
  if (isWindowsArch()) {
    D("This is Windows arch!");
    $gRootDir = "E:/Coding/Android/";     
    $refProjList = \@projList_win; 
    
  } elsif (isCygwinArch()) {
    D("This is Cygwin arch!");
    $gRootDir = "/E/Coding/Android/";     
    $refProjList = \@projList_win; 
    
  } else {
    D("This is Linux arch!");
    $gRootDir = "/media/Coding/And/";      
    $refProjList = \@projList_lnx;
  }
  #D(@$refProjList);
  
  while (1) {
    print_usage();
    my $option = <STDIN>;   chomp $option;
    D("option is $option") ;
    next if (!defined $option);
    
    if ('1' eq $option) {
      pull_github($refProjList);
    } elsif ('2' == $option) {
      push_github($refProjList);
    } elsif ('0' == $option) {
      P("Exiting...");
      exit 1;
      
    } else {
    }
  }
}

sub pull_github { 
  my ($refProjList) = @_;  D(@$refProjList);
  foreach my $dire (@$refProjList) {
    my $cmdStr = "cd $gRootDir/$dire; ";
    $cmdStr .= "git pull github master; ";
    
    my $cnt = 0;
    while (runSysCmd($cmdStr) != 0  && $cnt<10) {
      sleep(3);
      $cnt++;
    }
  }
}

sub push_github { 
  my ($refProjList) = @_;  D(@$refProjList);
  foreach my $dire (@$refProjList) {
    my $cmdStr = "cd $gRootDir/$dire; ";
    $cmdStr .= "git add -A; git commit -a -m 'no commit'; ";
    $cmdStr .= "git push github master; ";
    
    while (runSysCmd($cmdStr) != 0) {
      sleep(3);
    }
  }
}

sub print_usage {
  print"\n";
  printf("*** Function SELECTOR ***\n");
  printf("* 1. Pull GitHub        *\n");
  printf("* 2. Push GitHub        *\n");
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
