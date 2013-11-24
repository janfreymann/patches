use strict;

print "\n---------------------------- \n";
print "Spatial Sound Rerouting \n";
print "Based on DBAP\n";

print "\nJan Fremyann Nov 2013\n";
print "http://cpsnd.wordpress.com\n";

print "\n---------------------------- \n\n\n";

my ($fileA, $lengthA, $spkCountA, @xA, @yA, @xB, @yB, $spkCountB, $rolloff, $resize);
my $k;

my $readConf = getInput("Use previous config?", "y");

if($readConf eq "y") { goto READLASTCONFIG; }

print "Routing multichannel input from config A to config B\n";
print "Config A:\n";

$fileA = getInput("Filename of multichannel file A", "a.wav");
my $lengthString = getInput("Length of file A", "3m0s");

my ($minutes, $seconds) = split(/m/, $lengthString);
chop($seconds);
$lengthA = $minutes * 60 + $seconds;

$spkCountA = getInput("Number of speakers in config A", 4);

for($k = 0; $k < $spkCountA; $k++) {
	my $indx = $k+1;
	($xA[$k], $yA[$k]) = split(/ /, getInput("Coordinates of speaker/channel A $indx", "0 0"));
}

my $spkCountB = getInput("Number of speakers in config B", 4);

for($k = 0; $k < $spkCountB; $k++) {
	my $indx = $k+1;
	($xB[$k], $yB[$k]) = split(/ /, getInput("Coordinates of speaker/channel B $indx", "0 0"));
}

$rolloff = getInput("Amplitude rolloff in location B between 6 db and 1 db:", "4");
$resize = getInput("Do you want to scale and center speaker configurations?", "y");


goto CONFIGDONE;

READLASTCONFIG:

print "Reading last config.\n";

open(IN, "lastConf.txt") or die "Cannot open lastConf.txt for input: $!\n";

$fileA = <IN>; chomp($fileA);
$lengthA = <IN>; chomp($lengthA);
$spkCountA = <IN>; chomp($spkCountA);
for($k = 0; $k < $spkCountA; $k++) {
	my $line = <IN>; chomp($line);
	($xA[$k], $yA[$k])  = split(/ /, $line);
}	
$spkCountB = <IN>; chomp($spkCountB);
for($k = 0; $k < $spkCountB; $k++) {
	my $line = <IN>; chomp($line);
	($xB[$k], $yB[$k])  = split(/ /, $line);
}	

$rolloff = <IN>; chomp($rolloff);
$resize = <IN>; chomp($resize);

close IN;

CONFIGDONE:

my ($minA, $maxA, $minB, $maxB) = (9999999999, -9999999999, 9999999999, -9999999999);

print "Rerouting with the following configuration:\n";

print "In $fileA: $spkCountA speakers with coordinates\n";
for($k = 0; $k < $spkCountA; $k++) {
	if($xA[$k] > $maxA) { $maxA = $xA[$k]; }
	if($xA[$k] < $minA) { $minA = $xA[$k]; }
	if($yA[$k] > $maxA) { $maxA = $yA[$k]; }
	if($yA[$k] < $minA) { $minA = $yA[$k]; }	
	print "($xA[$k] $yA[$k]) ";
}
print "\n";
print "Outputting to out.wav: $spkCountB speakers with coordinates\n";
for($k = 0; $k < $spkCountB; $k++) {
	if($xB[$k] > $maxB) { $maxB = $xB[$k]; }
	if($xB[$k] < $minB) { $minB = $xB[$k]; }
	if($yB[$k] > $maxB) { $maxB = $yB[$k]; }
	if($yB[$k] < $minB) { $minB = $yB[$k]; }	
	print "($xB[$k] $yB[$k]) ";
}
print "\n";

print "Amplitude rolloff is " . $rolloff . "db\n";

print "Press ENTER to continue. Otherwise, press CTLR+C\n";
my $dummy = <STDIN>;

&saveConfig;

print "Saved config to file lastConf.txt\n";

#do preparing computations:

#normalize all coordinates to range 0..1
if($resize eq "y") {
	for($k = 0; $k < $spkCountA; $k++) {
		$xA[$k] = ($xA[$k] - $minA) / ($maxA - $minA);
		$yA[$k] = ($yA[$k] - $minA) / ($maxA - $minA);
	}
	for($k = 0; $k < $spkCountB; $k++) {
		$xB[$k] = ($xB[$k] - $minB) / ($maxB - $minB);
		$yB[$k] = ($yB[$k] - $minB) / ($maxB - $minB);	
	}
}

&printSpeakers;

#compute amplitudes for each sound source (speaker in config A) for each speaker in config B

my @amps;
my ($i, $j);

my $a = $rolloff / (6.0205999132);

for($i = 0; $i < $spkCountA; $i++) {

	#compute k first:
	my $sum = 0;
	for($j = 0; $j < $spkCountB; $j++) {
		$sum += 1 / (d($xA[$i], $yA[$i], $xB[$j], $yB[$j]) ** (2 * $a));
	}
	
	my $k = 1 / sqrt($sum);
	
	#compute amplitude of $xA[$i], $yA[$i] for each speaker $j in config B
	
	for($j = 0; $j < $spkCountB; $j++) {
		$amps[$i][$j] = $k / (d($xA[$i], $yA[$i], $xB[$j], $yB[$j]) ** $a);
		# DEBUG print "from $i to $j: $amps[$i][$j] " . d($xA[$i], $yA[$i], $xB[$j], $yB[$j]) . " $a \n";
	}
}

#generate csound code

open(CSD, ">reroute.csd") or die "Cannot open reroute.csd for output: $!\n";

print CSD <<'EOT';
<CsoundSynthesizer>
<CsOptions>

</CsOptions>

<CsInstruments>
; example written by Iain McCurdy

sr      =       44100
ksmps   =       32
EOT

print CSD "nchnls  =       $spkCountB\n";

print CSD "instr 1\n";

#a1, a2      diskin2  "a.wav", 1, 0, 0
my $sep = "";
for($i = 0; $i < $spkCountA; $i++) {
	print CSD $sep . "aA$i";
	$sep = ", ";
}
print CSD ' diskin2 "' . $fileA . '", 1, 0, 0' . "\n";

#;compute ab1, ab2,... here using coords and dbap

for($j = 0; $j < $spkCountB; $j++) {
	print CSD "aB$j sum ";
	my $sep = "";
	for($i = 0; $i < $spkCountA; $i++) {
		print CSD $sep . "($amps[$i][$j] * aA$i)";
		$sep = ", ";
	}
	print CSD "\n";
}

print CSD "outs ";
$sep = "";
for($j = 0; $j < $spkCountB; $j++) {
	print CSD $sep . "aB$j";
	$sep = ", ";
}
print CSD "\n";

#        outs      a1, a2          ; send audio to outputs

print CSD << 'EOT';
  endin

</CsInstruments>

<CsScore>
EOT
print CSD "i 1 0 $lengthA\n";
print CSD << 'EOT';
e
</CsScore>

</CsoundSynthesizer>
EOT

close CSD;

print "Saved csound code to reroute.csd\n";

#todo: run csound

my $runCsound = getInput("Do you want to run Csound now?" , "y");

if($runCsound eq "y") {
	print "Executing " . "csound reroute.csd -o reroute_$fileA" . "\n";
	system("csound reroute.csd -o reroute_$fileA");
	print "Output saved to reroute_$fileA\n";
}

print "See you!\n";

sub getInput {
	my ($msg, $default) = @_;
	
	print "$msg ($default): ";
	my $in = <STDIN>;
	chomp($in);
	if($in eq "") { return $default; }
	else { return $in; }
}
sub saveConfig {
	open(OUT, ">lastConf.txt") or die "Cannot open lastConf.txt for output: $!\n";
	print OUT $fileA . "\n";
	print OUT $lengthA . "\n";
	print OUT $spkCountA . "\n";
	for($k = 0; $k < $spkCountA; $k++) {
		print OUT "$xA[$k] $yA[$k]\n";
	}	
	print OUT $spkCountB . "\n";
	for($k = 0; $k < $spkCountB; $k++) {
		print OUT "$xB[$k] $yB[$k]\n";
	}	
	print OUT $rolloff . "\n";
	print OUT $resize . "\n";
	close OUT;
}

sub d {
	my ($x1, $y1, $x2, $y2) = @_;
	my $blur = 0.01; # setting spatial blur to 0.01
	return sqrt(($x1 - $x2) * ($x1 - $x2) + ($y1 - $y2) * ($y1 - $y2) + $blur*$blur);
}

sub printSpeakers { #display speaker configs in ASCII style
	my $scaleX = 40;
	my $scaleY = 24;
	my %toPrint;
	if($resize eq "y") { # this is easy, coordinates between 0 and 1
		for(my $i = 0; $i < $spkCountA; $i++) {
			my $x = int($xA[$i] * $scaleX);
			my $y = int($yA[$i] * $scaleY);
			my $n =  "A$i";
			$toPrint{"$x:$y"} .= $n;
		}
		for(my $i = 0; $i < $spkCountB; $i++) {
			my $x = int($xB[$i] * $scaleX);
			my $y = int($yB[$i] * $scaleY);
			my $n =  "B$i";
			$toPrint{"$x:$y"} .= $n;
			print "$x:$y $n\n";
		}
	}
	else { # this is ugly
		my $gMin = min($minA, $minB);
		my $gMax = max($maxA, $maxB);
		my $size = $gMax - $gMin;
		
		for(my $i = 0; $i < $spkCountA; $i++) {
			my $x = int((($xA[$i]-$gMin) / $size) * $scaleX);
			my $y = int((($yA[$i]-$gMin) / $size) * $scaleY);
			my $n =  "A$i";
			$toPrint{"$x:$y"} .= $n;
		}
		for(my $i = 0; $i < $spkCountB; $i++) {
			my $x = int((($xB[$i]-$gMin) / $size) * $scaleX);
			my $y = int((($yB[$i]-$gMin) / $size) * $scaleY);
			my $n =  "B$i";
			$toPrint{"$x:$y"} .= $n;
		}		
	}
	
	my $carry = 0;
	for(my $y = 0; $y <= $scaleY; $y++) {
		for(my $x = 0; $x <= $scaleX; $x++) {	
			if(exists($toPrint{"$x:$y"})) { 
				print $toPrint{"$x:$y"}; 
				$carry += length($toPrint{"$x:$y"});
			}
			elsif($carry == 0) { print "-"; }
			else { # $carry > 0
				$carry--;
			}
		}
		$carry = 0;
		print "\n";
	}
	
}
sub min {
	my ($a, $b) = @_;
	if($a < $b) { return $a } else { return $b }
}
sub max {
	my ($a, $b) = @_;
	if($a > $b) { return $a } else { return $b }
}