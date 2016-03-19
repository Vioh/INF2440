/** 
 * Brukernavn: syhd
 * INF2440 (Vaar 2016) - Oblig1
 */
import java.util.concurrent.*;
import java.util.*;

public class Oblig1 {
	int n;
	final int n_trials = 9; //numbers of trials --> should be odd
	final int n_elems = 40; //number of results that must be found
	
	/* Variables for keeping data for processing */
	final long seed; //seed for random number generator
	int[] data;
	int[] control; //control variable, containing results from Array.sort()
	               //this will be compared to results from sequential and
				   //parallel solutions to see if there are any mismatches
	
	/* Variables to be used in parallel solution */
	int n_cores;      //number of cores
	int n_threads;    //number of threads
	int n_per_thread; //number of elements per thread
	int[] results;    //results for parallel solution will be stored
	CyclicBarrier barrier;

	/* Variables for keeping statistics */
	boolean[] mismatchPara; //true if parallel solution gives wrong results
	boolean[] mismatchSeq ; //true if sequential solution gives wrong results
	double[] timeSort;      //time for Array.sort() method in millisecond (ms)
	double[] timePara;      //time for parallel solution in millisecond (ms)
	double[] timeSeq;       //time for sequential solution in millisecond (ms)

	/** Constructor */
	public Oblig1(int n, int threads) {
		this.n = n;
		seed = System.nanoTime();
		data = new int[n];
		control = new int[n_elems];
		
		n_cores = Runtime.getRuntime().availableProcessors();
		if(threads == 0) { n_threads = n_cores; }
		else { n_threads = threads; }
		
		mismatchSeq = new boolean[n_trials];
		mismatchPara = new boolean[n_trials];
		timeSort = new double[n_trials];
		timePara = new double[n_trials];
		timeSeq = new double[n_trials];
	}
	
	/** Main */
	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Usage: Oblig1 <n> <#threads>");
			System.out.print("Enter 0 for <#threads> if you want it to be ");
			System.out.print("chosen automatically.\n");
			return;
		}
		Oblig1 experiment = new Oblig1(Integer.parseInt(args[0]), 
		                               Integer.parseInt(args[1]));
		System.out.print("Please wait! Report will be printed ");
		System.out.print("out once execution is done.\n");
		experiment.execute();
	}
	
	/** Perform the experiment 'n_trials' time */
	public void execute() {
		for(int i = 0; i < n_trials; i++) {
			solveSort(i);
			solveSeq(i);
			solvePara(i);
		} printReport();
	}
	
	/** Print out the report of the experiment to the terminal */
	public void printReport() {
		for(int i = 0; i < n_trials; i++) {
			System.out.println("Trial " + (i+1));
			System.out.printf("- Array.sort() time: %6.2fms\n", timeSort[i]);
			System.out.printf("- Sequential time  : %6.2fms ", timeSeq[i]);
			System.out.println(mismatchInfo(mismatchSeq[i]));
			System.out.printf("- Parallel time    : %6.2fms ", timePara[i]);
			System.out.println(mismatchInfo(mismatchPara[i]));
		} 
		Arrays.sort(timeSort);
		Arrays.sort(timeSeq);
		Arrays.sort(timePara);
		int median = n_trials / 2;
		double speedup = timeSeq[median] / timePara[median];
		
		System.out.println("\nOverall statistics:");
		System.out.println("- #cores="+n_cores+", #threads="+n_threads+", n="+n);
		System.out.printf("- Median Array.sort() = %.2fms\n", timeSort[median]);
		System.out.printf("- Median sequential   = %.2fms\n", timeSeq[median]);
		System.out.printf("- Median parallel     = %.2fms\n", timePara[median]);
		System.out.printf("- Speed up            = %.2f\n\n", speedup);
	}
	
	/** Return mismatch information as a string */
	private String mismatchInfo(boolean mismatch) {
		if(mismatch) { return "(mismatch!)"; }
		return "(no mismatch!)";
	}

	
	
	/** Switch places for 2 elements in index 'i' and 'j' */
	private void switchPlaces(int[] a, int i, int j) {
		int tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}
	
	/** 
	 * Insertion sort (descending order) for the first 'n_elems' elements
	 * @param a array to be sorted
	 * @param lower_limit lower limit of the sorting scope
	 * @param lastElemOnly true if the sort focuses on the last element only
	 */
	private void insertSort(int[] a, int lower_limit, boolean lastElemOnly) {
		int k, tmp;
		int pos; //current position of the sort
		int startPos, lastPos = lower_limit + n_elems - 1;
		if(lastElemOnly) { 
			startPos = lastPos-1; 
		} else {
			startPos = lower_limit;
		}
		for(pos = startPos; pos < lastPos; pos++) {
			tmp = a[pos+1];
			k = pos;
			
			while(k >= lower_limit && a[k] < tmp) {
				a[k+1] = a[k--];	
			} a[k+1] = tmp;
	}}
	
	/** Generate data randomly using the 'seed' variable */
	private void generateData() {
		Random r = new Random(seed);
		for(int i = 0; i < n; i++) {
			data[i] = r.nextInt(n);
	}}
	
	
	
	/**
	 * Solve the problem using Array.sort() method
	 * @param trial trial number
	 */
	private void solveSort(int trial) {
		generateData();
		
		long time = System.nanoTime();
		Arrays.sort(data);
		time = System.nanoTime() - time;
		timeSort[trial] = time / 1000000.0;
		
		//Pick out the biggest numbers and put them into the 'control' variable
		for(int i = 1; i <= n_elems; i++) {
			control[i-1] = data[n-i];
	}}
	
	/**
	 * Solve the problem sequentially using the A2 algorithm
	 * @param trial trial number
	 */
	private void solveSeq(int trial) {
		generateData();
		
		long time = System.nanoTime();
		insertSort(data, 0, false); //sort the first 'n_elems' elements
		for(int i = n_elems; i < n; i++) {
			if(data[i] > data[n_elems-1]) {
				switchPlaces(data, i, (n_elems-1));
				insertSort(data, 0, true); //put last element in sorted order
		}} 
		time = System.nanoTime() - time;
		timeSeq[trial] = time / 1000000.0;
		
		//Check for mismatch
		for(int i = 0; i < n_elems; i++) {
			if(data[i] != control[i]) { mismatchSeq[trial] = true; }
	}}
	
	/**
	 * Solve the problem in parallel using a modifed A2 algorithm
	 * @param trial trial number
	 */
	private void solvePara(int trial) {
		/* Initialize necessary variables, and generate the data */
		n_per_thread = n / n_threads;
		results = new int[n_threads*n_elems]; //filtered data from all threads
		barrier = new CyclicBarrier(n_threads); //barrier before final sorting
		generateData();
		
		/* Start the algorithm and measure the time */
		long time = System.nanoTime();
		for(int i = 1; i < n_threads; i++) {
			(new Thread(new Parallel(i))).start();
		} try {
			Thread t0 = new Thread(new Parallel(0)); //the first thread (id=0).
			t0.start(); //since t0 has to wait for all other threads to finish,
			t0.join();  //the main thread needs to wait for only thread t0
		} catch(Exception e) { return; }
		time = System.nanoTime() - time;
		timePara[trial] = time / 1000000.0;
		
		/* Check for mismatch */
		for(int i = 0; i < n_elems; i++) {
			if(results[i] != control[i]) { mismatchPara[trial] = true; }
	}}
	
	
	
	/**
	 * The parallel algorithm will be as follow:
	 * - Each thread filters out 'n_elems' biggest elements in its own
	 *   respective scope
	 * - These filtered elements will then be put into 'results' array
	 * - The first thread (id=0) will then sort the 'results' array in
	 *   order to yield the top 'n_elems' search results
	 */
	private class Parallel implements Runnable {
		int id, index, low, high; //low <= scope < high
		public Parallel(int id) { this.id = id; }
		
		public void run() {
			/* Define the scope */
			low = id * n_per_thread;
			if(id == n_threads-1) { high = n; } //last thread
			else { high = low + n_per_thread; }
			
			/* Find the biggest 'n_elems' elements */
			insertSort(data, low, false);
			for(int i = low+n_elems; i < high; i++) {
				if(data[i] > data[low+n_elems-1]) {
					switchPlaces(data, i, (low+n_elems-1));
					insertSort(data, low, true); //sort the new element
			}}
			/* Put the biggest elements into the 'results' array */
			index = id * n_elems;
			for(int i = 0; i < n_elems; i++) {
				results[index+i] = data[low+i];
			}
			try { barrier.await(); } 
			catch(Exception e) { return; }
			
			/* We now use the first thread (id=0) to sort the 'results' array. */
			/* Since we know that all threads have already sorted their own    */
			/* elements in the descending order, we can easily optimize the    */
			/* procedure to sort the 'results' array.                          */
			if(id != 0) { return; }
			for(index = n_elems; index < results.length; index += n_elems) {
				for(int i = index; i < (index+n_elems); i++) {
					if(results[i] > results[n_elems-1]) {
						switchPlaces(results, i, (n_elems-1));
						insertSort(results, 0, true);
					} else { break; }
}}}}}