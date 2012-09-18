import java.util.HashSet;
import java.util.Iterator;

// examples

// Anytime Gade
// -d 554ajfh 6 -d 123bbcg 7 -k 1 15 -t "N55 4a.jfh E012 3b.bcg" -v a 0123 -v b 123456 -u abcfghj
// do med distance
// -d 554ajfh 6 -d 123bbcg 7 -k 1 15 -t "N55 4a.jfh E012 3b.bcg" -c "N55 42.111 E012 34.111" -cd 3000 

// Kan du cracke en gade, step 1:
// -k 4 4 -t "N cc ce.gjc, E fg fh.hcf    A=a, B=b" -d cccegjc 4 -d fgfhhcf 4 -v c 5 -v f 1 -v g 2

// step 2
// -k 7 7 -t "N dd jg.gcl, E ac bm.lfi   E=e, H=h" -v d 5 -v a 1 -v c 2 -v b 1 -v j 4 -v g 789 -v m 678

// step 3
// -k 8 8 -t "N 55 5b.cen, E 12 1f.ahi    H=h, J=j" -v b 0123 -v f 45678 -v n 9
// -k 8 8 -t "N 55 5b.cen, E 12 1f.ahi" -v b 0123 -v f 456789 -v n 9 -nn
// -k 8 8 -t "N 55 5b.cen, E 12 1f.ahi    H=h, J=j" -v b 0 -v f 7 -v n 9 -v c 6 -v e 7  

// step 4
// -k 1 10 -t "..." -ng

// Cyklen
// -k 6 6 -t "N55 4a.ifi E012 3d.hha" -v a 0 -v d 45 
// do, med brug af distance funktion
// -k 6 6 -t "N55 4a.ifi E012 3d.hha" -c "N55 40.741 E012 35.108" -cd 300    
// do, output kml fil som kan ses i f.eks. GE eller google maps.
//-k 6 6 -t "N55 4a.ifi E012 3d.hha" -c "N55 40.741 E012 35.108" -cd 300 -kml

// Example med positiv, negativ lister
// -k 6 6 -t "N55 4a.ifi E012 3d.hha" -v a 0 -v d 45 -y 33 -n 4 -t abcdef 

// Ligefrem gade
//  -t "N ab cd.efg E hij kl.mno" -k 1 16 -v a 5 -v h 0 -v c 012345  

// Bænken
// -k 12 12 -t "N55 4m.dla E012 2l.jjl"  -c "N55 49.072 E012 29.122" -cd 3000
// -k 12 12 -t "N55 4m.dla E012 2l.jjl"  -c "N55 49.072 E012 29.122" -cd 3000 -kml

// 1855.639001 m per N minut
// 1044.796791 m per E minut
// Derefter sqrt(diff^2 + diff^2)

// Don't add this

public class gadeSolver {
	// Calculate the distance in meters between two coordinate strings.
	// Coordinate strings are assumed to have the format 
	// Ndd mm.sss Eddd mm.sss
	// The error handling is NON EXISTING... BE CAREFUL
	private static int calc_dist(String c1, String c2) {
		double n1, n2, e1, e2, nd, ed, d;
		
		n1 = Float.parseFloat(c1.substring(4,10));
		e1 = Float.parseFloat(c1.substring(16,22));
		n2 = Float.parseFloat(c2.substring(4,10));
		e2 = Float.parseFloat(c2.substring(16,22));
		
		// TODO: this is not good enough.... needs adjustment...
		// This is on a branch - v2
		nd = (n2-n1) * 1855.639001;
		ed = (e2-e1) * 1044.796791;
		d = Math.sqrt(nd*nd + ed * ed);
		
		//System.out.println(c1 + " " + c2 + ": " + d);
		return (int)d;
	}
	
	private static char highest_char(String s, char higher_than) {
		char maxChar=higher_than;
		for (int i=0; i<s.length(); i++) {
			char c=s.charAt(i);
			if ((c>='a') && (c<='z') && (c>maxChar))
				maxChar = c;
		}
		return maxChar;
	}
	
	private static int complete(int []d_value, int k) {
		int digits=k, i, digit_to_add;
		
		// check present values, add values not there..
		for (i=0, digit_to_add=0; digit_to_add<10;) {
			if ((i<k) && (d_value[i] < digit_to_add)) {
				i++;  // checking smaller numbers right now...
				continue;
			}
			
			if ((i<k) && (d_value[i] == digit_to_add)) { 
				digit_to_add++; // digit already there
				i++;
				continue;
			} 
			
			// add digit
			d_value[digits++] = digit_to_add;
			digit_to_add++;
		}
		
		return digits;
	}
	
	private static int rDigitSum(int n) {
		if (n<10)
			return n;
		else
			return rDigitSum((n % 10) + rDigitSum(n / 10));
	}

	private static boolean check(int k, int[] d_value, int digits, int d_checks,
			String[] d_check_str, int[] d_check_value, 
			int v_checks, char[] v_check, String[] v_check_str, 
			int u_checks, String[] u_check_str,
			String d_present, String d_not_present) {
		// First let's count the number of digit instances 0...k-1...
		int[] digit_count = new int[10];
		for (int i=0; i<10; i++)
			digit_count[i] = 0;
		for (int i=0; i<k; i++)
			digit_count[d_value[i]]++;
		
		// check for not_present
		for (int i=0; i<d_not_present.length(); i++) {
			if (digit_count[d_not_present.charAt(i) - '0']>0)
				return false;
		}
		
		// check for present
		for (int i=0; i<d_present.length(); i++) 
			digit_count[d_present.charAt(i) - '0']--;
		for (int i=0; i<10; i++) {
			if (digit_count[i] < 0)
				return false;
		}
		
		// Check any value constraints
		for (int vc = 0; vc < v_checks; vc++) {
			int value = d_value[v_check[vc]-'a'];
			int i;
			for (i=0; i<v_check_str[vc].length(); i++) {
				if (value == v_check_str[vc].charAt(i) - '0') // ok
					break;
			}
			if (i == v_check_str[vc].length())  // we failed to find the value 
				return false;
		}
		
		
		// Check reduced digital root
		for (int dc = 0; dc<d_checks; dc++) {
			int n=0;
			// first extract values given by string
			for (int i=0; i<d_check_str[dc].length(); i++) {
				char c = d_check_str[dc].charAt(i);
				if ((c>='a') && (c<='z'))
					n += d_value[c - 'a'];
				else if ((c>='0') & (c<='9'))
					n += c - '0';
			}
			
			//System.out.println("check. " + d_check_str[dc] + " " + d_check_value[dc] + " " + n + " " + rDigitSum(n));
			
			if (rDigitSum(n) != d_check_value[dc])
				return false;
		}
		
		// Check uniqueness
		for (int uc = 0; uc<u_checks; uc++) {
			int[] u_value = new int[10];
			int n, j;
			for (int i=0; i<10; i++)
				u_value[i] = -1;
			
			for (int i=0; i<u_check_str[uc].length(); i++) {
				char c = u_check_str[uc].charAt(i);
				n = d_value[c - 'a'];
				if (n>9)
					return false;
				//System.out.println(n);
				if (u_value[n] != -1)
					return false;
				u_value[n] = 1;
			}
		}
		
		return true;
	}

	private static String output(int []d_value, int digits, String template) {
		String result ="";
		
		for (int i=0; i<template.length(); i++) {
			char c = template.charAt(i);
			if ((c>='a') && (c<='z') && (c-'a' < digits)) { 
				// substitute
				result += d_value[c-'a'];
			} else {
				// copy
				result += c;
			}
		}
		return result;
	}
	
	private static void kml_output_start()  {
		System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		System.out.println("  <kml xmlns=\"http://www.opengis.net/kml/2.2\">");
		System.out.println("  <Document>");
	}
	
	private static void kml_output_solution(String coor, int sol) {
		// N55 55.555 E012 23.234
		// 0123456789012345678901
		//           1         2
		double n, e;
		
		n = Float.parseFloat(coor.substring(1,3)) +
				Float.parseFloat(coor.substring(4,10)) / 60.0;
		e = Float.parseFloat(coor.substring(13,15)) +
				Float.parseFloat(coor.substring(16,22)) / 60.0;
		
		System.out.println(" <Placemark><name>" + coor + "</name>");
		System.out.println("   <Point><coordinates>" + e + "," + n + "</coordinates></Point>");
		System.out.println(" </Placemark>");
	}
	
	private static void kml_output_final() {
		System.out.println("</Document>");
		System.out.println("</kml>");
	}
		
	public static void main(String[] args) {
		// search for gade d(0)... d(k-1) d(k) ... d(m)
		// where k = number of digits obtained from the problem
		// d(k) .. d(m) represent the digits not contained in d(0) ... d(k-1). These digits are unique and in ascending order.
		
		// d(0) = a, d(1) = b, etc.
		
		// To search for a gade, typically you want to check the result against one or more reducedDigitalRoot based on a,b,c,....
		// This is specificed in argument(s) "-d={letters},sum",   e.g.    -d abgj 6 -d abbd 8
		
		// For searching, we need to make assumptions about the value of k, i.e. the number of digits what are resulting from the problem.
		// this is specified by argument k (range of known digits to search), e.g. -k 4 6    -k 5 5
		
		// We also allow constraining the values to be found by giving certain possible values 
		// This is done using argument -v,    e.g.       -v a 0123    -v b 123456
		
		// The resulting values may be inserted into a template. Set using -t 
		// The default template is abcdefghijklmnopqrstuvxyz
		
		// Set list of digits (including rep) that MUST be before k, e.g.  -y  1138 
		// Set list of digits that MUST NOT be before k             ,e.g.  -n 67
		
		// Option -nn suppressed the default printing of "sol# :" before each solution.
		
		// Option -ng to print number of possible gades for each k
		
		// -c "Nxx xx.yyy Exxx xx.yyy"
		// -cd 600
		
		// -kml
		
        int i=0, k_min=0, k_max=0, k, min_digits;
        String arg;
        HashSet<String> solutions = new HashSet<String>();
        
        boolean print_sol_num = true;
        boolean print_no_gades = false;
        
        int[] d_value = new int[100];

        int d_checks = 0;
        String[] d_check_str = new String[10];
        int[] d_check_value = new int[10];
        
        int v_checks = 0;
        char[] v_check = new char[10];
        String[] v_check_str = new String[10];
        
        int u_checks = 0;
        String[] u_check_str = new String[10];

        String d_present = "";
        String d_not_present = "";
        
        String template = "abcefghijklmnopqrstuvxyz";
        
        String coor = "N55 47.214 E012 21.301";
        int coor_dist = 0;
        
        boolean kml = false;

        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];

            if (arg.equals("-k")) {
            	k_min = Integer.parseInt(args[i++]);
            	k_max = Integer.parseInt(args[i++]);
         	
            } else if (arg.equals("-d")) {
            	d_check_str[d_checks] = args[i++];
            	d_check_value[d_checks++] = Integer.parseInt(args[i++]);
            } else if (arg.equals("-v")) {
            	v_check[v_checks] = args[i++].charAt(0);
            	v_check_str[v_checks++] = args[i++];
            } else if (arg.equals("-u")) {
            	u_check_str[u_checks++] = args[i++];
            } else if (arg.equals("-t")) {
            	template = args[i++];
            } else if (arg.equals("-y")) {
            	d_present = args[i++];
            } else if (arg.equals("-n")) {
            	d_not_present = args[i++];
            } else if (arg.equals("-c")) {
            	coor = args[i++];
            } else if (arg.equals("-cd")) {
            	coor_dist = Integer.parseInt(args[i++]);
            } else if (arg.equals("-nn")) {
            	print_sol_num = false;
            	i++;
            } else if (arg.equals("-ng")) {
            	print_no_gades = true;
            	i++;
            } else if (arg.equals("-kml")) {
            	kml = true;
            	i++;
            } else {
            	System.out.println("gadeSolver -k k_min k_max {-d root_string root_value} [-t template]");
            	System.exit(1);
            }
        }
        
        // Find highest char in template and d_checks... We need to get at least this many digits from our gade. 
        char maxChar = highest_char(template, 'a');
        for (i=0; i<d_checks; i++)
        	maxChar = highest_char(d_check_str[i], maxChar);
        min_digits = (maxChar - 'a') +1 ;
        
        // main k loop
        int no_gades;
        for (k=k_min; k<=k_max; k++) {
        //	System.out.println("k=" + k);
        	
        	// Now, given k, we need to try all the possible values.
        	// First we iterate the known values
        	no_gades = 0;
        	for (i=0; i<100; i++) 
        		d_value[i] =0;
        	int digits = 0;
        	for (i=0;;) {
        		
        		if (i==k) {
        			// ok, all values from the problem have been set. Time to complete with the missing digits 
        			// (in order) and check the solution ....
        			digits = complete(d_value, k);
        			no_gades++;
        			
        			if ((digits >= min_digits) && check(k, d_value, digits, d_checks, d_check_str, d_check_value, 
        					v_checks, v_check, v_check_str, u_checks, u_check_str, d_present, d_not_present))
        				solutions.add(new String(output(d_value, digits, template)));
        			
        			// now, backtrack, and restart 
        			for (i--; i>=0 && d_value[i]==9; i--);
        			if (i<0) 
        				break; // done
        			d_value[i]++;
        			continue;
        		}
        		
        		i++;
        		d_value[i] = d_value[i-1];
        	}	      		
        	
        	// done with k
        	if (print_no_gades)
        		System.out.println("For k=" + k + " there are " + no_gades + " possible gades.");
        }
        
        int sol=1;
        int dist=0;
        String sol_string;
        if (kml)
        	kml_output_start();
        
    	for (Iterator iterator = solutions.iterator(); iterator.hasNext();) {
    		
    		sol_string = iterator.next().toString();
    		if (coor_dist >0) {
    			// Distance check
    			dist = calc_dist(sol_string, coor);
    			if (dist > coor_dist)
    				continue;
     		}
    		if (print_sol_num && !kml)
    			System.out.print(sol + ": ");
    		
    		if (kml)
    			kml_output_solution(sol_string, sol);
    		else
    			System.out.println(sol_string + " ");
    		sol++;
    	}
    	
    	if (kml)
    		kml_output_final();
	}
}