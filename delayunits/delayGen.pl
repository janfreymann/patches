# Jan Freymann
# November 2013
# cpsnd.wordpress.com

use strict;

my $infile = shift(@ARGV);

open(IN, $infile) or die "Cannot open $infile: $!\n";

my $ms = <IN>;
chomp($ms);

my $dtime = 0;

print '#N canvas 256 209 495 341 10;' . "\n";
print '#X obj 177 10 inlet~' . ";\n";   #0
print '#X obj 201 279 outlet~' . ";\n";  #1
print '#X obj 274 276 outlet~' . ";\n";  #2
print '#X obj 210 32 loadbang' . ";\n"; #3

my $objIndx = 4;

my $lastDelRead = 0;

my @conns = ();

my @delWrites = ();
my @delReads = ();

while(<IN>) {
    chomp;
    my $line = $_;
    if($line eq "feedback") { last; }
    my @tmp = split("", $line);
    my $range = scalar(@tmp);
    my ($delAmp, $delPan, $delTime);
    $dtime += $ms;
    
    #create DelRead, DelWrite Object
    my $delName = '$0-line'	. $dtime;
    my $x = int(rand(200)) + 100;
    my $y = int(rand(200)) + 100;
    
    print '#X obj ' . " $x $y " . 'delwrite~ ' . "$delName $ms;\n";
    push(@delWrites, $objIndx);
    push(@conns, "#X connect " . $lastDelRead . " 0 " . $objIndx . " 0;\n");
    $objIndx++;
    $x = int(rand(200)) + 100;
    $y = int(rand(200)) + 100;
    print '#X obj ' . " $x $y " . 'delread~ ' . "$delName $ms;\n";
    $lastDelRead = $objIndx;
    push(@delReads, $objIndx);
    $objIndx++;
    
    for(my $i = 0; $i < $range; $i++) {
        if($tmp[$i] ne '-') {
            $delAmp = $tmp[$i] / 10;
            $delPan = ($i / $range) * 90 - 45;
            #create Pan-Object
            $x = int(rand(200)) + 100;
    	    $y = int(rand(200)) + 100;
    	    print "#X obj $x $y *~ $delAmp;\n";
    	    push(@conns, "#X connect $lastDelRead 0 $objIndx 0;\n");
    	    $objIndx++;
    	    $x = int(rand(200)) + 100;
    	    $y = int(rand(200)) + 100;
            print "#X obj $x $y pan~;\n";
            push(@conns, "#X connect " . ($objIndx-1) . " 0 $objIndx 0;\n");
            push(@conns, "#X connect $objIndx 0 1 0;\n");
            push(@conns, "#X connect $objIndx 1 2 0;\n");
            $objIndx++;
            $x = int(rand(200)) + 100;
    	    $y = int(rand(200)) + 100;
            print "#X msg $x $y $delPan;\n";
            push(@conns, "#X connect 3 0 $objIndx 0;\n");
            push(@conns, "#X connect $objIndx 0 " . ($objIndx-1) . " 1;\n");
            $objIndx++;
        }
    }
}

#process feedbacks:
while(<IN>) {
    chomp;

    my ($from, $to, $amp) = split;
    $from -= 2; # convert from line numbers to indices
    $to -= 2;
    $from = $delReads[$from];
    $to = $delWrites[$to];

    my $x = int(rand(200)) + 100;
    my $y = int(rand(200)) + 100;    
    print "#X obj $x $y *~ $amp;\n";
    push(@conns, "#X connect $from 0 $objIndx 0;\n"); 
    push(@conns, "#X connect $objIndx 0 $to 0;\n"); 
    $objIndx++;
}

close IN;

print join("", @conns) . "\n";
