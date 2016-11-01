package Stubs;

import java.util.ArrayList;

import com.knoll.tos.F684102JE;
import com.knoll.tos.ListObjects;

public class Test03 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		F684102JE error = new F684102JE();
		F684102JE error1 = new F684102JE();
		ArrayList<F684102JE> wtf = new ArrayList<F684102JE>();
		ListObjects lo = new ListObjects();
		
		error.setQxitm(123);
		error.setQxlitm("test 123");
		error.setQxuser("you you");
		wtf.add(error);
		lo.setBunch(wtf);
		
		error1.setQxitm(456);
		error1.setQxlitm("test 456");
		error1.setQxuser("i me me mine");
		wtf.add(error1);
		lo.setBunch(wtf);
		
		ArrayList<F684102JE> gotit = lo.getBunch();
		
		for (F684102JE to : gotit){
			
			System.out.println(to.getQxitm());
			System.out.println(to.getQxlitm());
			System.out.println(to.getQxuser());
			
		}
		
	}

}
