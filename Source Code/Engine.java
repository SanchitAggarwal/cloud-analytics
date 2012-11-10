

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;



@WebServlet("/analytics")
public class Engine extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	static int init = 0;
	static int Job_id = 0;
	static ArrayList<Algo_args> Algo_args_List;
	static ArrayList<Framework_Info> FrameWork_List;
	static String PWD;
	static ArrayList<Jobs> Job_List = new ArrayList<Jobs>();
	static void initialize() throws IOException
	{
		Algo_args_List = new ArrayList<Algo_args>();
		FrameWork_List = new ArrayList<Framework_Info>();
		//Initialise Algo_args_List by opening the respective File open in an append mode
		//Create PB
		BufferedReader br = new BufferedReader(new FileReader(new File("/usr/local/make")));
		PWD=br.readLine();
		br.close();
		File dir = new File(PWD+"/algo_files/");
		String[] FrkList = dir.list();
		for(int i = 0; i < FrkList.length; i++)
		{
			Framework_Info temp = new Framework_Info();
			temp.framework_name = FrkList[i];
			temp.algorithms = new ArrayList<String>();
			temp.algo_files = new ArrayList<String>();
			dir = new File(PWD+"/algo_files/"+FrkList[i]);
			String[] temp_List = dir.list();
			for(i = 0; i < temp_List.length; i++)
				temp.algo_files.add(temp_List[i]);
			FrameWork_List.add(temp);
		}
		//System.out.println(PWD);
		br = new BufferedReader(new FileReader(new File(PWD+"/Algo_arguments")));
		String argument = "";
		while((argument=br.readLine()) != null)
		{
			Algo_args temp = new Algo_args();
			temp.frmwrk_name = argument.substring(0, argument.indexOf(' '));
			//System.out.println(temp.frmwrk_name);
			argument = argument.substring(argument.indexOf(' ') + 1);
			temp.algo_name = argument.substring(0, argument.indexOf(' '));
			argument = argument.substring(argument.indexOf(' ') + 1);
			for(int i = 0; i < FrameWork_List.size(); i++)
				if(FrameWork_List.get(i).framework_name.equals(temp.frmwrk_name))
					FrameWork_List.get(i).algorithms.add(temp.algo_name);
			temp.algo_Location = argument.substring(0, argument.indexOf(' '));
			argument = argument.substring(argument.indexOf(' ') + 1);
			temp.no_of_comp_args = Integer.parseInt(argument.substring(0, argument.indexOf(' ')));
			argument = argument.substring(argument.indexOf(' ') + 1);
			temp.no_of_opt_args = Integer.parseInt(argument.substring(0, argument.indexOf(' ')));
			argument = argument.substring(argument.indexOf(' ') + 1);
			temp.comptargs = new Arg_Container[temp.no_of_comp_args];
			temp.opttargs = new Arg_Container[temp.no_of_opt_args];
			for(int i = 0; i < temp.no_of_comp_args; i++)
			{
				temp.comptargs[i] = new Arg_Container();
				temp.comptargs[i].arg_name = argument.substring(0, argument.indexOf(' '));
				argument = argument.substring(argument.indexOf(' ') + 1);
				temp.comptargs[i].arg_nick = argument.substring(0, argument.indexOf(' '));
				argument = argument.substring(argument.indexOf(' ') + 1);
				temp.comptargs[i].arg_type = argument.substring(0, argument.indexOf(' '));
				argument = argument.substring(argument.indexOf(' ') + 1);
			}
			for(int i = 0; i < temp.no_of_opt_args; i++)
			{
				temp.opttargs[i] = new Arg_Container();
				temp.opttargs[i].arg_name = argument.substring(0, argument.indexOf(' '));
				argument = argument.substring(argument.indexOf(' ') + 1);
				temp.opttargs[i].arg_nick = argument.substring(0, argument.indexOf(' '));
				argument = argument.substring(argument.indexOf(' ') + 1);
				temp.opttargs[i].arg_type = argument.substring(0, argument.indexOf(' '));
				argument = argument.substring(argument.indexOf(' ') + 1);
			}
			temp.PB = br.readLine();
			Algo_args_List.add(temp);
		}
		init = 1;
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		JSONObject result = new JSONObject();
		System.out.println(init);
		if(init == 0)
		{
			initialize();
			result.put("", "");
			response.getWriter().println(result);
			return;
		}
		response.setContentType("application/json");
		String file = request.getRequestURI()+"?"+request.getQueryString();
		//System.out.println(file);
		file = file.substring(11);
		try {
			result = main(file);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.getWriter().println(result);
	}
	static JSONObject main(String file) throws IOException, InterruptedException
	{
		//System.out.println("here");
		if(file.startsWith("execute"))
		{
			file=file.substring(8);
			if(file.startsWith("return_args"))
				return retrn_arg_exist_algo(file.substring(22));
			else if(file.startsWith("algo"))
			{
				String alg_name = file.substring(15, file.indexOf('&'));
				file = file.substring(file.indexOf('&'));
				ArrayList<Arg_Container> arg = new ArrayList<Arg_Container>();
				while(file.startsWith("&"))
				{
					file = file.substring(1);
					Arg_Container temp = new Arg_Container();
					temp.arg_nick = file.substring(0, file.indexOf('='));
					if(file.contains("&"))
					{
						temp.arg_val = file.substring(file.indexOf('=') + 1, file.indexOf('&'));
						file = file.substring(file.indexOf('&'));
					}
					else
						temp.arg_val = file.substring(file.indexOf('=') + 1);
					arg.add(temp);
				}
				return exec_exist_algo(alg_name, arg);
			}
			else if(file.startsWith("script"))
				return exec_script(file.substring(file.indexOf('=') + 1));
		}
		else if(file.startsWith("add"))
		{
			file=file.substring(4);
			if(file.startsWith("framework"))
			{
				String frkname = file.substring(25, file.indexOf('&'));
				file = file.substring(file.indexOf('&')+1);
				String cmpfile = file.substring(15, file.indexOf('&'));
				file = file.substring(file.indexOf('&')+1);
				return add_new_frmwrk(frkname, cmpfile, file.substring(15));
			}
			else if(file.startsWith("algo"))
			{
				//System.out.println(file);
				String frkname = file.substring(20, file.indexOf('&'));
				file = file.substring(file.indexOf('&')+1);
				String algname = file.substring(15, file.indexOf('&'));
				file = file.substring(file.indexOf('&')+1);
				String algfile = file.substring(15, file.indexOf('&'));
				file = file.substring(file.indexOf('&')+1);
				String algrun = file.substring(14, file.indexOf('&'));
				algrun = algrun.replaceAll("%20", " ");
				file = file.substring(file.indexOf('&')+1);
				int no_comp_args = Integer.parseInt(file.substring(26, file.indexOf('&')));
				file = file.substring(file.indexOf('&')+1);
				int no_opt_args = Integer.parseInt(file.substring(25, file.indexOf('&')));
				//System.out.println(frkname + " " + algname + " " + algfile + " " + no_comp_args + " " + no_opt_args);
				//System.out.println(algrun);
				file = file.substring(file.indexOf('&'));
				Arg_Container[] comp_arg = new Arg_Container[no_comp_args];
				int comp_arg_count = 0;
				Arg_Container[] opt_arg = new Arg_Container[no_opt_args];
				int opt_arg_count = 0;
				while(file.startsWith("&"))
				{
					file = file.substring(1);
					Arg_Container temp = new Arg_Container();
					temp.arg_type = file.substring(file.indexOf('(') + 1, file.indexOf(')'));
					temp.arg_name = file.substring(file.indexOf('=')+1, file.indexOf('('));
					temp.arg_nick = file.substring(file.indexOf('(', file.indexOf('=')) + 1, file.indexOf(')', file.indexOf('=')));
					//System.out.println(temp.arg_name + " " + temp.arg_nick);
					//System.out.println(file.startsWith("comp") + " " + file);
					if(file.startsWith("comp"))
					{
						comp_arg[comp_arg_count] = temp;
						//System.out.println(temp.arg_name);
						comp_arg_count++;
					}
					else if(file.startsWith("opt"))
					{
						opt_arg[opt_arg_count++] = temp;
						opt_arg_count++;
					}
					if(file.contains("&"))
						file = file.substring(file.indexOf('&'));
				}
				return add_new_algo(frkname, algname, algfile, algrun, no_comp_args, no_opt_args, comp_arg, opt_arg);
			}
		}
		else if(file.startsWith("list"))
		{
			file = file.substring(5);
			if(file.startsWith("framework"))
				return framework_list();
			else if(file.startsWith("algorithm"))
				return algorithm_list();
			else if(file.startsWith("combined"))
				return combined_list();
			else if(file.startsWith("specific"))
				return sepcific_framework_list(file.substring(file.indexOf('=') + 1));
			else if(file.startsWith("all_algo_files"))
				return all_algo_file_list();
			else if(file.startsWith("algo_files"))
				return algo_files(file.substring(file.indexOf('=') + 1));
		}
		else if(file.startsWith("status"))
		{
			file = file.substring(7);
			return job_status(Integer.parseInt(file.substring(file.indexOf('=') + 1)));
		}
		else if(file.startsWith("remove"))
		{
			file = file.substring(7);
			if(file.startsWith("framework"))
			{
				String framework_name = file.substring((file.indexOf('=') + 1), file.indexOf('&'));
				file = file.substring(file.indexOf('&', file.indexOf(framework_name)) + 1);
				String uninstall_script_path = file.substring(22);
				return remove_framework(framework_name, uninstall_script_path);
			}
			if(file.startsWith("algo"))
			{
				String framework_name = file.substring((file.indexOf('=') + 1), file.indexOf('&'));
				file = file.substring(file.indexOf('&', file.indexOf(framework_name)) + 1);
				String algo = file.substring(15, file.indexOf('&'));
				file = file.substring(file.indexOf('&')+1);
				String uninstall_script_path = file.substring(22);
				return remove_algorithm(framework_name, algo, uninstall_script_path);
			}
		}
		return null;
	}
	static JSONObject retrn_arg_exist_algo(String algo)
	{
		JSONObject jso = new JSONObject();
		for(int i = 0; i < Algo_args_List.size(); i++)
			if(Algo_args_List.get(i).algo_name.equalsIgnoreCase(algo))
			{
				jso.put("no_of_compulsory_args", Algo_args_List.get(i).no_of_comp_args);
				jso.put("no_of_optional_args", Algo_args_List.get(i).no_of_opt_args);
				JSONObject comp_JSONOBj = new JSONObject();
				JSONObject opt_JSONOBj = new JSONObject();
				for(int j = 0; j < Algo_args_List.get(i).no_of_comp_args; j++)
					comp_JSONOBj.put(Algo_args_List.get(i).comptargs[j].arg_name, Algo_args_List.get(i).comptargs[j].arg_nick+"("+Algo_args_List.get(i).comptargs[j].arg_type+")");
				for(int j = 0; j < Algo_args_List.get(i).no_of_opt_args; j++)
					opt_JSONOBj.put(Algo_args_List.get(i).opttargs[j].arg_name, Algo_args_List.get(i).opttargs[j].arg_nick+"("+Algo_args_List.get(i).opttargs[j].arg_type+")");
				jso.put("comp_args", comp_JSONOBj);
				jso.put("opt_args", opt_JSONOBj);
				return jso;
			}
		jso.put("Error", "algo_name_does_not_match");
		return jso;
	}
	static JSONObject exec_exist_algo(String algo, ArrayList<Arg_Container> arg) throws IOException
	{
		JSONObject jso = new JSONObject();
		ArrayList <Arg_Container> compargs = new ArrayList<Arg_Container>();
		ArrayList <Arg_Container> optargs = new ArrayList<Arg_Container>();
		for(int i = 0; i < Algo_args_List.size(); i++)
			if(Algo_args_List.get(i).algo_name.equalsIgnoreCase(algo))
			{
				for(int j = 0; j < arg.size(); j++)
				{
					if(match(arg.get(j).arg_nick, i) == 0)
					{
						jso.put("Error", "Nick Wrong");
						return jso;
					}
					else if(match(arg.get(j).arg_nick, i) == 1)
						compargs.add(arg.get(i));
					else if(match(arg.get(j).arg_nick, i) == 2)
						optargs.add(arg.get(i));
				}
				//copy in HDFS if required
				ArrayList<String> commands;
				ProcessBuilder pb;
				String source = "/usr/local/test"+Integer.toString(init*init);
				if(new File(source).exists() && new File(source).isDirectory())
				{
					commands = new ArrayList<String>();
					commands.add("fusermount");
					commands.add("-u");
					commands.add(source);
					//System.out.println(commands);
					pb = new ProcessBuilder(commands);
					pb.start();
				}
				else if(!new File(source).exists())
					new File(source).mkdir();
				commands = new ArrayList<String>();
				commands.add("sshfs");
				int j, inindex = 0, outindex = 0;
				for(j = 0; j < arg.size(); j++)
					if(arg.get(j).arg_nick.contains("out"))
						outindex = j;
				for(j = 0; j < arg.size(); j++)
				{
					//System.out.println(arg.get(j).arg_nick);
					if(arg.get(j).arg_nick.contains("in"))
					{
						commands.add(arg.get(j).arg_val);
						inindex = j;
						break;
					}
				}
				commands.add(source);
				//System.out.println(commands);
				pb = new ProcessBuilder(commands);
				pb.start();
				commands = new ArrayList<String>();
				commands.add("/usr/local/hadoop/bin/hadoop");
				commands.add("dfs");
				commands.add("-copyFromLocal");
				commands.add(source);
				commands.add("input"+Integer.toString(init));
				//System.out.println(commands);
				pb = new ProcessBuilder(commands);
				pb.start();
				//PB for respective file
				List<String> word = Arrays.asList(Algo_args_List.get(i).PB.split(" "));
				ArrayList<String> wordList = new ArrayList<String>();
				for(j = 0; j < word.size(); j++)
					wordList.add(word.get(j));
				//ArrayList<String>wordList = (ArrayList<String>)word;
				//System.out.println(wordList);
				int k;
				for (k = 0; k < wordList.size(); k++)
				{
					//System.out.println(wordList.get(k));
					if(wordList.get(k).endsWith(".jar") || wordList.get(k).endsWith(".R"))
						if(!(wordList.get(k).startsWith("/") && (new File(wordList.get(k)).exists())))
						{
							wordList.add(k, PWD+"/algo_files/"+wordList.get(k));
							wordList.remove(k+1);
						}
					for(j = 0; j < arg.size(); j++)
						if(wordList.get(k).equals(arg.get(j).arg_nick))
						{
							//System.out.println(k + " " + wordList.get(k) + " " + arg.get(j).arg_val + " " + j + " " + inindex);
							//System.out.println(wordList);
							wordList.remove(wordList.get(k));
							if(j == inindex)
								wordList.add(k, "input"+Integer.toString(init));
							else
								wordList.add(k, arg.get(j).arg_val);
							//System.out.println(wordList);
							break;
						}
					if(j == arg.size())
					{
						//System.out.println(wordList.get(k) + " " + arg.get(j).arg_nick);
						for(j = 0; j < Algo_args_List.get(i).opttargs.length; j++)
							if(Algo_args_List.get(i).opttargs[j].arg_nick.equals(wordList.get(k)))
							{
								wordList.remove(k);
								//System.out.println(wordList);
								k--;
								break;
							}
					}
				}
				//System.out.println(wordList);
				pb = new ProcessBuilder(wordList);
				pb.start();
				commands = new ArrayList<String>();
				commands.add("jps");
				pb = new ProcessBuilder(commands);
				Process p = pb.start();
				String line;
				int job_id = 0;
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null)
				{
					//System.out.println(line);
					if(line.contains("RunJar") || line.contains("process information unavailable") || line.contains("PlatformName"))
					{
						if(line.contains("RunJar"))
						{
							int m;
							for(m = 0; m < Job_List.size(); m++)
								if(Job_List.get(m).id == Integer.parseInt(line.substring(0, line.indexOf(' '))))
									break;
							if(m == Job_List.size())
							{
								job_id = Integer.parseInt(line.substring(0, line.indexOf(' ')));
								break;
							}
						}
						//System.out.println("Running");
						commands = new ArrayList<String>();
						commands.add("jps");
						pb = new ProcessBuilder(commands);
						p = pb.start();
						input = new BufferedReader(new InputStreamReader(p.getInputStream()));
					}
				}
				Jobs temp = new Jobs();
				temp.id = job_id;
				temp.output_folder = arg.get(outindex).arg_val;
				temp.put = 0;
				Job_List.add(temp);
				jso.put("Job_id", job_id);
				init++;
			}
		return jso;
	}
	static JSONObject exec_script(String script_file_path) throws IOException, InterruptedException
	{
		JSONObject jso = new JSONObject();
		int endindex;
		for(endindex = script_file_path.length()-1; endindex >= 0; endindex--)
			if(script_file_path.charAt(endindex) == '/' || script_file_path.charAt(endindex) == ':')
				break;
		String filename = script_file_path.substring(endindex+1);
		//System.out.println(filename);
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("scp");
		commands.add(script_file_path);
		commands.add(PWD+"/run_scripts/"+filename);
		//System.out.println(commands);
		ProcessBuilder pb = new ProcessBuilder(commands);
		Process p = pb.start();
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while (input.readLine() != null)
		{
		}
		Process proc = Runtime.getRuntime().exec(new String[]
				{
				"/bin/sh", "-c", PWD+"/run_scripts/"+filename
				});
		proc.waitFor();
		jso.put(filename, 1);
		return jso;
	}
	static JSONObject add_new_frmwrk(String frmwrkname, String compressed_file_path, String install_script) throws IOException, InterruptedException
	{
		JSONObject jso = new JSONObject();
		//PB
		//copy the framework folder
		int endindex;
		for(endindex = install_script.length()-1; endindex >= 0; endindex--)
			if(install_script.charAt(endindex) == '/' || install_script.charAt(endindex) == ':')
				break;
		String filename = install_script.substring(endindex+1);
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("scp");
		commands.add("-r");
		commands.add(compressed_file_path);
		commands.add(PWD+"/framework_zips/");
		//System.out.println(commands);
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.start();
		commands = new ArrayList<String>();
		commands.add("scp");
		commands.add(install_script);
		commands.add(PWD+"/install_scripts/"+filename);
		//System.out.println(commands);
		pb = new ProcessBuilder(commands);
		pb.start();
		/*commands = new ArrayList<String>();
		commands.add("cd");
		commands.add("/usr/local");
		System.out.println(commands);
		pb = new ProcessBuilder(commands);
		pb.start();*/
		//run the script file
		commands = new ArrayList<String>();
		commands.add("scp");
		commands.add("-r");
		commands.add(compressed_file_path);
		commands.add("/usr/local/");
		//System.out.println(commands);
		pb = new ProcessBuilder(commands);
		pb.start();
		Process proc = Runtime.getRuntime().exec(new String[]
				{
				"/bin/sh", "-c", PWD+"/install_scripts/"+filename
				});
		proc.waitFor();
		//System.out.println(commands);
		//create a folder for adding algorithms for this framework
		commands = new ArrayList<String>();
		commands.add("mkdir");
		commands.add(PWD+"/algo_files/"+frmwrkname);
		//System.out.println(commands);
		pb = new ProcessBuilder(commands);
		pb.start();
		Framework_Info temp = new Framework_Info();
		temp.framework_name = frmwrkname;
		temp.algorithms = new ArrayList<String>();
		FrameWork_List.add(temp);
		jso.put(frmwrkname, 1);
		return jso;
	}
	static JSONObject add_new_algo(String frkname, String algname, String algfile, String algo_run, int no_comp_args, int no_opt_args, Arg_Container[] comp_arg, Arg_Container[] opt_arg) throws IOException
	{
		JSONObject jso = new JSONObject();
		int check;
		for(check = 0; check < FrameWork_List.size(); check++)
			if(FrameWork_List.get(check).framework_name.equalsIgnoreCase(frkname))
				break;
		if(check == FrameWork_List.size())
			jso.put("Error", "Framework not found");
		else
		{
			FrameWork_List.get(check).algorithms.add(algname);
			//String host = algfile.substring(0, algfile.indexOf(':'));
			//PB to add jar in the respective folder
			int endindex;
			for(endindex = algfile.length()-1; endindex >= 0; endindex--)
				if(algfile.charAt(endindex) == '/' || algfile.charAt(endindex) == ':')
					break;
			String filename = algfile.substring(endindex+1);
			Algo_args temp = new Algo_args();
			if(!(new File(PWD+"/algo_files/"+frkname+"/"+filename)).exists())
			{
				ArrayList<String> commands = new ArrayList<String>();
				commands.add("scp");
				commands.add("-r");
				commands.add(algfile);
				commands.add(PWD+"/algo_files/"+frkname);
				ProcessBuilder pb = new ProcessBuilder(commands);
				pb.start();
				FrameWork_List.get(check).algo_files.add(filename);
			}
			//add in the file
			//add in Algo_args_List<>
			//System.out.println(PWD+"/Algo_arguments");
			FileWriter fw = new FileWriter(new File(PWD+"/Algo_arguments"), true);
			fw.write(frkname+" "+algname+" "+frkname+"/"+filename+" "+Integer.toString(no_comp_args)+" "+Integer.toString(no_opt_args)+" ");
			temp.frmwrk_name = frkname;
			temp.algo_name = algname;
			temp.algo_Location = frkname+"/"+filename;
			temp.no_of_comp_args = no_comp_args;
			temp.no_of_opt_args = no_opt_args;
			temp.comptargs = new Arg_Container[temp.no_of_comp_args];
			temp.opttargs = new Arg_Container[temp.no_of_opt_args];
			//System.out.println(comp_arg.length);
			for(int i = 0; i < no_comp_args; i++)
			{
				//System.out.println(comp_arg[i].arg_name + " " + comp_arg[i].arg_nick);
				fw.write(comp_arg[i].arg_name+" "+comp_arg[i].arg_nick+" ");
				temp.comptargs[i] = new Arg_Container();
				temp.comptargs[i].arg_name = comp_arg[i].arg_name;
				temp.comptargs[i].arg_nick = comp_arg[i].arg_nick;
			}
			for(int i = 0; i < no_opt_args; i++)
			{
				fw.write(opt_arg[i].arg_name+" "+opt_arg[i].arg_nick+" ");
				temp.opttargs[i] = new Arg_Container();
				temp.opttargs[i].arg_name = opt_arg[i].arg_name;
				temp.opttargs[i].arg_nick = opt_arg[i].arg_nick;
			}
			temp.PB = algo_run;
			fw.write("\n"+algo_run);
			fw.close();
			Algo_args_List.add(temp);
			jso.put(algname, 1);
		}
		return jso;
	}
	static JSONObject job_status(int job_id) throws IOException
	{
		//System.out.println(Job_List.size());
		JSONObject jso = new JSONObject();
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("jps");
		ProcessBuilder pb = new ProcessBuilder(commands);
		Process p = pb.start();
		String line;
		int run = 0;
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((line = input.readLine()) != null)
			if(line.contains(Integer.toString(job_id)) && line.contains("RunJar"))
			{
				jso.put("status", "running");
				run = 1;
			}
		if(run == 0)
		{
			int i;
			for(i = 0; i < Job_List.size(); i++)
			{
				//System.out.println(Job_List.get(i).id);
				if(job_id == Job_List.get(i).id)
					break;
			}
			if(i == Job_List.size())
			{
				jso.put("Error", "invald job_id");
				return jso;
			}
			String dest = "/usr/local/test"+Integer.toString(job_id);
			if(Job_List.get(i).put == 0)
			{
				if (new File(dest).exists())
					delete_file(new File(dest));
				new File(dest).mkdir();
				commands = new ArrayList<String>();
				commands.add("/usr/local/hadoop/bin/hadoop");
				commands.add("dfs");
				commands.add("-copyToLocal");
				commands.add("/user/hduser/"+Job_List.get(i).output_folder);
				commands.add(dest);
				//System.out.println(commands);
				pb = new ProcessBuilder(commands);
				pb.start();
				Job_List.get(i).put = 1;
			}
			jso.put("output_file_name", Job_List.get(i).output_folder);
			jso.put("output_file_location", dest);
		}
		return jso;
	}
	static JSONObject framework_list()
	{
		JSONObject jso = new JSONObject();
		for(int i = 0; i < FrameWork_List.size(); i++)
				jso.put(Integer.toString(i+1), FrameWork_List.get(i).framework_name);
		return jso;
	}
	static JSONObject algorithm_list()
	{
		JSONObject jso = new JSONObject();
		for(int i = 0; i < Algo_args_List.size(); i++)
			jso.put(Integer.toString(i + 1), Algo_args_List.get(i).algo_name);
		return jso;
	}
	static JSONObject combined_list()
	{
		JSONObject jso = new JSONObject();
		for(int i = 0; i < FrameWork_List.size(); i++)
		{
			JSONObject AlgoJSONObject = new JSONObject();
			for(int j = 0; j < FrameWork_List.get(i).algorithms.size(); j++)
				AlgoJSONObject.put(Integer.toString(j + 1), FrameWork_List.get(i).algorithms.get(j));
			jso.put(FrameWork_List.get(i).framework_name, AlgoJSONObject);
		}
		return jso;
	}
	static JSONObject sepcific_framework_list(String framework_name)
	{
		JSONObject jso = new JSONObject();
		for(int i = 0; i < FrameWork_List.size(); i++)
			if(FrameWork_List.get(i).framework_name.equals(framework_name))	
				for(int j = 0; j < FrameWork_List.get(i).algorithms.size(); j++)
					jso.put(Integer.toString(j + 1), FrameWork_List.get(i).algorithms.get(j));
		return jso;
	}
	static JSONObject all_algo_file_list()
	{
		JSONObject jso = new JSONObject();
		for(int i = 0; i < FrameWork_List.size(); i++)
		{
			JSONObject AlgoJSONObject = new JSONObject();
			for(int j = 0; j < FrameWork_List.get(i).algo_files.size(); j++)
				AlgoJSONObject.put(Integer.toString(j + 1), FrameWork_List.get(i).algo_files.get(j));
			jso.put(FrameWork_List.get(i).framework_name, AlgoJSONObject);
		}
		return jso;
	}
	static JSONObject algo_files(String framework_name)
	{
		JSONObject jso = new JSONObject();
		for(int i = 0; i < FrameWork_List.size(); i++)
			if(FrameWork_List.get(i).framework_name.equals(framework_name))	
				for(int j = 0; j < FrameWork_List.get(i).algo_files.size(); j++)
					jso.put(Integer.toString(j + 1), FrameWork_List.get(i).algo_files.get(j));
		return jso;
	}
	static JSONObject remove_framework(String framework_name, String uninstall_script_path) throws IOException, InterruptedException
	{
		JSONObject jso = new JSONObject();
		int endindex;
		for(endindex = uninstall_script_path.length()-1; endindex >= 0; endindex--)
			if(uninstall_script_path.charAt(endindex) == '/' || uninstall_script_path.charAt(endindex) == ':')
				break;
		String filename = uninstall_script_path.substring(endindex+1);
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("scp");
		commands.add(uninstall_script_path);
		commands.add(PWD+"/remove_scripts/"+filename);
		//System.out.println(commands);
		ProcessBuilder pb = new ProcessBuilder(commands);
		Process p = pb.start();
		BufferedReader inpt = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while (inpt.readLine() != null)
		{
		}
		Process proc = Runtime.getRuntime().exec(new String[]
				{
				"/bin/sh", "-c", PWD+"/remove_scripts/"+filename
				});
		proc.waitFor();
		for(int i = 0; i < FrameWork_List.size(); i++)
			if(FrameWork_List.get(i).equals(framework_name))	
				FrameWork_List.remove(i);
		for(int i = 0; i < Algo_args_List.size(); i++)
			if(Algo_args_List.get(i).frmwrk_name.equals(framework_name))	
				Algo_args_List.remove(i);
		delete_file(new File(PWD+"/algo_files/"+framework_name));
		BufferedReader br = new BufferedReader(new FileReader(new File(PWD+"/Algo_arguments")));
		FileWriter fw = new FileWriter(new File(PWD+"/Algo_arguments_temp"));
		String input;
		int i = 0;
		while((input = br.readLine()) != null)
		{
			if(input.startsWith(framework_name))
				br.readLine();
			else if(i == 0)
			{
				fw.write(input + "\n" + br.readLine());
				i++;
			}
			else
				fw.write("\n" + input + "\n" + br.readLine());
		}
		br.close();
		fw.close();
		delete_file(new File(PWD+"/Algo_arguments"));
		new File(PWD+"/Algo_arguments_temp").renameTo(new File(PWD+"/Algo_arguments"));
		jso.put(framework_name, 1);
		return jso;
	}
	static JSONObject remove_algorithm(String framework_name, String algo, String uninstall_script_path) throws IOException, InterruptedException
	{
		JSONObject jso = new JSONObject();
		int endindex;
		for(endindex = uninstall_script_path.length()-1; endindex >= 0; endindex--)
			if(uninstall_script_path.charAt(endindex) == '/' || uninstall_script_path.charAt(endindex) == ':')
				break;
		String filename = uninstall_script_path.substring(endindex+1);
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("scp");
		commands.add(uninstall_script_path);
		commands.add(PWD+"/remove_scripts/"+filename);
		//System.out.println(commands);
		ProcessBuilder pb = new ProcessBuilder(commands);
		Process p = pb.start();
		BufferedReader inpt = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while (inpt.readLine() != null)
		{
		}
		Process proc = Runtime.getRuntime().exec(new String[]
				{
				"/bin/sh", "-c", PWD+"/remove_scripts/"+filename
				});
		proc.waitFor();
		for(int i = 0; i < FrameWork_List.size(); i++)
			if(FrameWork_List.get(i).equals(framework_name))
				for(int j = 0; j < FrameWork_List.get(i).algorithms.size(); j++)
					if(FrameWork_List.get(i).algorithms.get(j).equals(algo))
						FrameWork_List.get(i).algorithms.remove(j);
		for(int i = 0; i < Algo_args_List.size(); i++)
			if(Algo_args_List.get(i).frmwrk_name.equals(framework_name) && Algo_args_List.get(i).algo_name.equals(algo))
				Algo_args_List.remove(i);
		BufferedReader br = new BufferedReader(new FileReader(new File(PWD+"/Algo_arguments")));
		FileWriter fw = new FileWriter(new File(PWD+"/Algo_arguments_temp"));
		int i = 0;
		String input;
		while((input = br.readLine()) != null)
		{
			if(input.substring(input.indexOf(' ') + 1).startsWith(algo))
				br.readLine();
			else if(i == 0)
			{
				fw.write(input + "\n" + br.readLine());
				i++;
			}
			else
				fw.write("\n" + input + "\n" + br.readLine());
		}
		br.close();
		fw.close();
		delete_file(new File(PWD+"/Algo_arguments"));
		new File(PWD+"/Algo_arguments_temp").renameTo(new File(PWD+"/Algo_arguments"));
		jso.put(algo, 1);
		return jso;
	}
	static int match(String arg_nick, int num)
	{
		for(int i = 0; i < Algo_args_List.get(num).comptargs.length; i++)
			if(Algo_args_List.get(num).comptargs[i].arg_nick.equals(arg_nick))
				return 1;
		for(int i = 0; i < Algo_args_List.get(num).opttargs.length; i++)
			if(Algo_args_List.get(num).opttargs[i].arg_nick.equals(arg_nick))
				return 2;
		return 0;
	}
	static void delete_file(File file) throws IOException
	{
		if(file.isDirectory())
			if(file.list().length==0)
				file.delete();
			else
			{
				//list all the directory contents
				String files[] = file.list();
				for (String temp : files)
				{
					//construct the file structure
					File fileDelete = new File(file, temp);
					//recursive delete
					delete_file(fileDelete);
				}
				//check the directory again, if empty then delete it
				if(file.list().length==0)
				{
					file.delete();
				}
			}
		else
			file.delete();
	}
}