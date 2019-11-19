package CMM;

import java.util.ArrayList;
import java.util.Vector;

public class SemanticAnalysis {
	//执行问题区间 田国庆
	public boolean sfirstMetBlock;
	public boolean sExcute;//是否执行
	public boolean sifJump;//while 中true代表要跳了（不执行） if else中默认为false
	public int swhileStart;
	public int swhileEnd;
	public int sifStart;
	public int selseStart;
	//执行问题区间 田国庆
	//4是否执行
//	public boolean IFE()//while 用于firstWExc
//	{
//		boolean fW = this.sExcute;
//		int falseNum = 0;
//		SemanticAnalysis temp = front;
//		while(temp != null){
//			if(temp.sExcute == false)
//			{
//				++falseNum;
//			}
//			temp = temp.front;
//		}
//		if(falseNum!=0)
//		{
//			return false;//无法执行
//		}else {
//			if(fW == true)
//			{
//				return true;
//			}else
//			{
//				return false;
//			}
//		}
//	}
	public int IFE()
	{
		int falseNum = 0;
		SemanticAnalysis temp = front;
		while(temp != null){
			if(temp.sExcute == false)
			{
				++falseNum;
			}
			temp = temp.front;
		}
		return falseNum;
	}
	//4是否执行
	public Vector<Record> Reco = new Vector<Record>();//原来有static

	//变量作用域
	public ArrayList<SemanticAnalysis> next = new ArrayList<>();	//下一深度//原来有static
	private SemanticAnalysis front;   //前一深度//原来有static

	public void addNext(SemanticAnalysis semanticAnalysis){
		next.add(semanticAnalysis);
	}//原来有static

	public void setFront(SemanticAnalysis _frontId){
		front = _frontId;
	}//原来有static

	public SemanticAnalysis getFront(){
		return front;
	}//原来有static
	//变量作用域

	//将申明的变量加入到Reco中
	public void addToReco(token t, int id)
	{
		Reco.add(new Record(id,t.content));
	}//原来有static
	
	//返回已经申明的变量的类型
	public int getType(token t )//原来有static
	{
		for(Record r : Reco)
		{
			if(t.content.equals(r.getName()))
			{
				return r.getId();
			}
		}
		//变量作用域
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					return r.getId();
				}
			}
			temp = temp.front;
		}
		//变量作用域
		return -1;//类型不匹配
	}

	//判断是否已经申明
	public boolean IfRep(token t)//原来有static
	{
		for(Record r : Reco)
		{
			if(r.getName().equals(t.content))
			{
				return true;
			}
		}
		/*if(t.kind>0)
		{
			addToReco(t,id);
		}*/
		return false;
	}
	//变量作用域
	//用于判断变量是否存在，需要判断所有深度
	public boolean IsExist(token t){
		for(Record r : Reco)
		{
			if(r.getName().equals(t.content))
			{
				return true;
			}
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					return true;
				}
			}
			temp = temp.front;
		}
		return false;
	}
	//变量作用域
	public void setArray(token t,int a)//原来有static
	{
		for(Record r : Reco)
		{
			if(r.getName().equals(t.content))
			{
				r.setFlag(true);
				r.setVol(a);
			}
		}
	}
	
	
	//查看real int 类型是否匹配 以及数组类型是否匹配
	public boolean NumMatch(int id, token t)//原来有static
	{
		for(Record r : Reco)
		{
			if(r.getName().equals(t.content))
			{
				if(r.getFlag()==false)
				{
					if(t.content!=null&&t.content.matches("^[0-9]+$"))
					{
						if(id == 25)
							return true;
						else 
							return false;
					}
					else {
						if(id == 26)
							return true;
						else 
							return false;
					}
				}
				else 
				{
					if(t.content!=null&&t.content.matches("(^[\\-0-9][0-9]*(.[0-9]+)?)$"))
						return false;
					else
						return true;
				}
			}
		}
		return false;
	}
	public boolean Num1(int id, token t)//原来有static
	{
		if(t.content!=null&&t.content.matches("^[0-9]+$"))
		{
			if(id == 25)
				return true;
			else 
				return false;
		}
		else {
			if(id == 26)
				return true;
			else 
				return false;
		}
	}
	
	//判断数组是否越界
	public boolean IfArrayOver(token t, int temp)//原来有static
	{
		for(Record r : Reco)
		{
			if(r.getName().equals(t.content)) 
			{
				if(r.getVol()>=temp)
					return false;
				else 
					return true;
			}
		}
		return false;
	}
	
	
	//判断是否除零
	public boolean IfDivZero(int temp)//原来有static
	{
		if(temp==0)
			return true;
		else
			return false;
	}
	
	
	//设置是否赋值标志
	public void setIfValued(boolean a, token t)//原来有static
	{
		for(Record r :Reco)
		{
			if(r.getName().equals(t.content))
				r.setIfValued(true);
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					r.setIfValued(true);
				}
			}
			temp = temp.front;
		}
	}
	
	public boolean getIfValued(token t)//原来有static
	{
		for(Record r :Reco)
		{
			if(r.getName().equals(t.content))
				return r.getIfValued();
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					return r.getIfValued();
				}
			}
			temp = temp.front;
		}
		return false;
	}
	//YUYI二期
	public void setArraySize(token t, int size)//原来有static
	{
		for(Record r:Reco)
		{
			if(r.getName().equals(t.content)) {
				r.setArraySize(size);
			}
		}
	}

	public int getArraySize(token t)//原来有static
	{
		for(Record r : Reco)
		{
			if(r.getName().equals(t.content))
				return r.getArraySize();
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					return r.getArraySize();
				}
			}
			temp = temp.front;
		}
		return 0;
	}

	public boolean getIsArray(token t)//原来有static
	{
		for(Record r : Reco)
		{
			if(r.getName().equals(t.content))
				return r.getIsShuzu();
			
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					return r.getIsShuzu();
				}
			}
			temp = temp.front;
		}
		return false;
	}

	//第四次迭代
	//变量赋值
	public void setvalue(token t, int a)
	{
		for(Record r :Reco)
		{
			if(r.getName().equals(t.content))
			{
				r.setIntV(a);
				return ;
			}
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					r.setIntV(a);
					return;
				}
			}
			temp = temp.front;
		}
	}
	public void setvalue(token t, float a)
	{
		for(Record r :Reco)
		{
			if(r.getName().equals(t.content))
			{
				r.setFloatV(a);
				return ;
			}
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					r.setFloatV(a);
					return;
				}
			}
			temp = temp.front;
		}
	}
	public void setvalue(token t, int a,int b)
	{
		for(Record r :Reco)
		{
			if(r.getName().equals(t.content))
			{
				r.setIntArrayV(a, b);
				return ;
			}
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					r.setIntArrayV(a, b);
					return ;
				}
			}
			temp = temp.front;
		}
	}
	public void setvalue(token t,float a, int b)
	{
		for(Record r :Reco)
		{
			if(r.getName().equals(t.content))
			{
				r.setFloatArrayV(a, b);
				return ;
			}
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					r.setFloatArrayV(a, b);
					return ;
				}
			}
			temp = temp.front;
		}
	}
	
	
	public int getIntV(token t)
	{
		for(Record r :Reco)
		{
			if(r.getName().equals(t.content))
			{
				//System.out.println(r.getIntV()+"dangqian");
				return r.getIntV();
			}
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					//System.out.println(r.getIntV()+"hahah");
					return r.getIntV();
				}
			}
			temp = temp.front;
		}
		return 0;
	}

	//4444
//	public int clean(token t)
//	{
//		for(Record r :Reco)
//		{
//			if(r.getName().equals(t.content))
//			{
//				System.out.println("找！"+r.getIntV());
//			}
//		}
//		SemanticAnalysis temp = front;
//		while(temp != null){
//			for(Record r : temp.Reco){
//				if(r.getName().equals(t.content))
//				{
//					System.out.println("找到"+r.getIntV());
//				}
//			}
//			temp = temp.front;
//			System.out.println("改变深度");
//		}
//		return 0;
//	}
	//4
	public int getIntArrayV(token t,int a)
	{
		for(Record r :Reco)
		{
			if(r.getName().equals(t.content))
			{
				return r.getIntArrayV(a);
			}
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					return r.getIntArrayV(a);
				}
			}
			temp = temp.front;
		}
		return 0;
	}
	public float getFloatV(token t)
	{
		for(Record r:Reco)
		{
			if(r.getName().equals(t.content))
			{
				return r.getFloatV();
			}
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					return r.getFloatV();
				}
			}
			temp = temp.front;
		}
		return 0;
	}
	public float getFloatArrayV(token t,int a)
	{
		for(Record r :Reco)
		{
			if(r.getName().equals(t.content))
			{
				return r.getFloatArrayV(a);
			}
		}
		SemanticAnalysis temp = front;
		while(temp != null){
			for(Record r : temp.Reco){
				if(r.getName().equals(t.content))
				{
					return r.getFloatArrayV(a);
				}
			}
			temp = temp.front;
		}
		return 0;
	}
}



//记录普通变量
class Record{
	private int id;//记录类型
	private String name;
	private boolean flag;
	private int vol = 20;
	private boolean ifValued;
	//变量作用域
	private int depth;   //变量所在的深度 从1开始
	//变量作用域
	private int arraySize;
	private boolean isShuzu;

	//第四次
	private int intV;
	private int[] intArrayV = new int[vol];
	private float floatV;
	private float[] floatArrayV = new float[vol];
	
	
	public Record(int id, String name)
	{
		super();
		this.id = id;
		this.name = name;
		this.isShuzu = false;//默认不是数组
		
	}
	public int getId() {
		return id;
	}
	public String getName()
	{
		return name;
	}
	public boolean getFlag()
	{
		return flag;
	}
	public int getVol()
	{
		return vol;
	}
	public boolean getIfValued()
	{
		return ifValued;
	}
	public void setFlag(boolean a)
	{
		flag = a;
	}
	public void setVol(int num)
	{
		vol = num;
	}
	public void setIfValued(boolean a)
	{
		ifValued = a;
	}
	//YUYI 二期
	public int getArraySize()
	{
		return arraySize;
	}
	public void setArraySize(int size)
	{
		arraySize = size;
		isShuzu = true;
	}
	public void setIsShuzu()
	{
		isShuzu = true;
	}
	public boolean getIsShuzu()
	{
		return isShuzu;
	}
	//变量作用域
	public void setDepth(int _depth){
		depth = _depth;
	}
	public int getDepth(){
		return depth;
	}
	//变量作用域

	//第四次
	public void setIntV(int a )
	{
		this.intV = a;
	}
	public void setIntArrayV( int a ,int b)
	{
		this.intArrayV[b] = a;
	}
	public void setFloatV(float a)
	{
		this.floatV = a;
	}
	public void setFloatArrayV(float a, int b)
	{
		this.floatArrayV[b] = a;
	}
	public int getIntV()
	{
		return intV;
	}
	public float getFloatV()
	{
		return floatV;
	}
	public int getIntArrayV(int a)
	{
		return intArrayV[a];
	}
	public float getFloatArrayV(int a)
	{
		return floatArrayV[a];
	}
}

