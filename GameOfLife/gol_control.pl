#Jan Freymann
#9/26/2013

use strict;
use Net::OpenSoundControl::Client;
use Net::OpenSoundControl::Server;

my $client = Net::OpenSoundControl::Client->new(
      Host => "127.0.0.1", Port => 7777)
      or die "Could not start client: $@\n";
	  
$client->send(['/hello', 'i', 1]);

#initialize cells

my @cells = ();

#read initial pattern from file:

my $filename = shift(@ARGV);

open(IN, $filename) or die "Cannot open file $filename: $!\n";

my $size = <IN>;
chomp($size);

#stuff for soundgen allocation (similar to poly object in pd)

my @available = (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36);
my %inuse = ();

sub allocateSound {
	my ($x, $y) = @_;
	
	if(scalar(@available) > 0) {
		my $indx = pop(@available);
		$inuse{"$x:$y"} = $indx;
		$client->send(['/cell/allocate', 'i', $indx, 'i', $x, 'i', $y]);
	}
	else { # perform voice stealing
		my @kk = keys(%inuse);
		my $k = pop @kk;
		my ($xx, $yy) = split(/:/, $k);
		&removeSound($xx, $yy);
		&allocateSound($x, $y);
	}
}

sub shutdown {
	my @coords = keys(%inuse);
	my $k;
	foreach $k (@coords) {
		my ($x, $y) = split(/:/, $k);
		&removeSound($x, $y);
	}
}

sub removeSound {
	my ($x, $y) = @_;
	if(exists($inuse{"$x:$y"})) {
		$client->send(['/cell/remove', 'i', $inuse{"$x:$y"}, 'i', $x, 'i', $y]);
		unshift(@available, $inuse{"$x:$y"});
		delete($inuse{"$x:$y"});
	}
}

system("cls");

my @newcells = ();

my $first = 1;

my $server = Net::OpenSoundControl::Server->new(
      Port => 7778, Handler => \&handler) or
      die "Could not start listening socket on port 7778: $@\n";
	  
	  $server->readloop;

sub handler {
	system("cls");
	
	if($first) {
		for(my $x = 0; $x < $size; $x++) {
			my $line = <IN>;
			chomp($line);
			my @tmp = split(//, $line);
			for(my $y = 0; $y < $size; $y++) {
				if($tmp[$y] eq "o") {
					$cells[$x][$y] = 1; #dead is 0, alive is 1
					allocateSound($x, $y);
				}
				else {
					$cells[$x][$y] = 0; #dead is 0, alive is 1
				}
			}
		}
		close IN;
	}

	my $count = 0;
	@newcells = ();
	for(my $x = 0; $x < $size; $x++) {
		for(my $y = 0; $y < $size; $y++) {
			if($cells[$x][$y]) { 
				print "o"; 
				$count++;
			}
			else { 
				print " "; 
			}
		}
		print "\n";
	}
	
	if($first) { $first = 0; return; }
	
	print "$count alive cells.\n";
	#my $cont = <STDIN>;
	#chomp($cont);
	#if($cont eq "q") { &shutdown; die "Bye!\n"; }

	for(my $x = 0; $x < $size; $x++) {
		for(my $y = 0; $y < $size; $y++) {
			my $alive = decideAlive($x, $y);
			if($cells[$x][$y] != $alive) { #something has changed, tell the patch
				if($alive) { #cell came to live
					&allocateSound($x, $y);
				}
				else { #bcell has died
					&removeSound($x, $y);
				}
			}			
			$newcells[$x][$y] = $alive;
		}
	}
	@cells = @newcells;
}

sub decideAlive { #do this for each cell
	my ($x, $y) = @_;
	
	# see https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life
	# for the rules
	if($cells[$x][$y] == 1) { #live cell
		if(getNeighbourAliveCount($x, $y) < 2) { return 0; }
		elsif(getNeighbourAliveCount($x, $y) > 3) { return 0; }
		else { return 1; }
	}
	else { #dead cell
		if(getNeighbourAliveCount($x, $y) == 3) { return 1; }
		else { return 0; }
	}
}
sub getNeighbourAliveCount {
	my ($x, $y) = @_;
	my $alive = 0;
	$alive += $cells[($x - 1) % $size][($y) % $size];
	$alive += $cells[($x) % $size][($y - 1) % $size];
	$alive += $cells[($x + 1) % $size][($y) % $size];
	$alive += $cells[($x) % $size][($y + 1) % $size];
	
	$alive += $cells[($x - 1) % $size][($y + 1) % $size];
	$alive += $cells[($x - 1) % $size][($y - 1) % $size];
	$alive += $cells[($x + 1) % $size][($y + 1) % $size];
	$alive += $cells[($x + 1) % $size][($y - 1) % $size];
	
	return $alive;
}
