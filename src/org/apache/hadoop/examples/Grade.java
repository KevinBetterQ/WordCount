package org.apache.hadoop.examples;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * @author KevinQi
 *
 */
public class Grade{
	
	public static class GradeMapper extends Mapper<Object, Text, Text, IntWritable>{
		
		private final static IntWritable one = new IntWritable(1);
		
		@Override
		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			StringTokenizer tokens = new StringTokenizer(line,"\n");
			while(tokens.hasMoreTokens()){
				String tmp = tokens.nextToken();
				StringTokenizer sz = new StringTokenizer(tmp);
				String name = sz.nextToken();
				float score = Float.valueOf(sz.nextToken());
				Text outName;
				if(score>=90&&score<=100) 
					outName = new Text("A");
				else if(score>=80&&score<=89) 
					outName = new Text("B");
				else if(score>=70&&score<=79) 
					outName = new Text("C");
				else if(score>=60&&score<=69) 
					outName = new Text("D");
				else
					outName = new Text("E");				
				
				//Text gname = new Text(name);
				//FloatWritable outScore  = new FloatWritable(score);
				context.write(outName, one);
			}
		}
		
	}
	
	public static class GradeReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
		
		private IntWritable result = new IntWritable();
		@Override
		protected void reduce(Text key, Iterable<IntWritable> value,Context context)
				throws IOException, InterruptedException {
			
			 int sum = 0;
		      for (IntWritable val : value) {
		        sum += val.get();
		      }
		      result.set(sum);
		      context.write(key, result);
		}
		
	}
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		
		Configuration conf = new Configuration();
		/*
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if(otherArgs.length<2){
			System.out.println("please input at least 2 arguments");
			System.exit(2);
		}
		*/
		String[]  otherArgs = new String[]{"input","output"};
		
		Job job = new Job(conf,"grade");
		job.setMapperClass(GradeMapper.class);
		job.setCombinerClass(GradeReducer.class);
		job.setReducerClass(GradeReducer.class);
		  job.setOutputKeyClass(Text.class);
		    job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		
		System.exit(job.waitForCompletion(true)?0:1);
		
	}
	
}
