package abc.tm.ast;

public aspect TmsmCount {
	
	// Capture all matrix operations
	pointcut newTM() : call(* regex_c.MakeSM());

	after() : newTM()
	{
		// Increment operation count
		++TMCount;
		System.err.println("So far: "+ TMCount +" tracematch statem achines");
	}


// Aspect constructor
TmsmCount() {
 TMCount = 0;}
	
// Count of all matrix operations
private static int TMCount;
}


}
