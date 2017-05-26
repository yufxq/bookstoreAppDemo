package com.example.fangxiaoqi.bookstoredemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import model.Book;
import zrc.widget.SimpleHeader;
import zrc.widget.ZrcListView;
import zrc.widget.ZrcListView.OnStartListener;

import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class bookActivity extends AppCompatActivity {

    private ZrcListView listView;
    private MyAdapter adapter;
    private Handler hh=new Handler();
	public static Book thisbook;
	private List<Book> books=new ArrayList<Book>();
	public static String ip="192.168.1.5:8080/BookStore";
	public void sendHttpPost(String getUrl, Book book) {
		HttpURLConnection urlConnection = null;
		URL url = null;
		try {
			url = new URL(getUrl);
			urlConnection = (HttpURLConnection) url.openConnection();//打开http连接
			urlConnection.setConnectTimeout(3000);//连接的超时时间
			urlConnection.setUseCaches(false);//不使用缓存
			//urlConnection.setFollowRedirects(false);是static函数，作用于所有的URLConnection对象。
			urlConnection.setInstanceFollowRedirects(true);//是成员函数，仅作用于当前函数,设置这个连接是否可以被重定向
			urlConnection.setReadTimeout(3000);//响应的超时时间
			urlConnection.setDoInput(true);//设置这个连接是否可以写入数据
			urlConnection.setDoOutput(true);//设置这个连接是否可以输出数据
			urlConnection.setRequestMethod("POST");//设置请求的方式
			urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置消息的类型
			urlConnection.connect();// 连接，从上述至此的配置必须要在connect之前完成，实际上它只是建立了一个与服务器的TCP连接
			Gson gson = new Gson();
			String jsonstr = gson.toJson(book);

			//------------字符流写入数据------------
			OutputStream out = urlConnection.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
			bw.write(jsonstr);//把json字符串写入缓冲区中
			bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
			out.close();
			bw.close();//使用完关闭

			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {//得到服务端的返回码是否连接成功

				//------------字符流读取服务端返回的数据------------
				InputStream in = urlConnection.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String str = null;
				StringBuffer buffer = new StringBuffer();
				while ((str = br.readLine()) != null) {//BufferedReader特有功能，一次读取一行数据
					buffer.append(str);
			}
			in.close();
			br.close();

		}

		} catch (Exception e) {

		} finally {
			Message msg = mHandler.obtainMessage();
			msg.what = 1;
			mHandler.sendMessage(msg);
			urlConnection.disconnect();//使用完关闭TCP连接，释放资源
		}

	}
	public  Handler mHandler = new Handler(){  //登录注册handler
		public void handleMessage(Message msg) {
			if(msg.what==1) {//添加完成
				((EditText)findViewById(R.id.title)).setText("");
				((EditText)findViewById(R.id.author)).setText("");
				((EditText)findViewById(R.id.price)).setText("");
				((EditText)findViewById(R.id.publisher)).setText("");

				refresh();
			}
			else if (msg.what==2){//查询完成
				adapter.notifyDataSetChanged();
				//listView.refresh();
				listView.setRefreshSuccess("加载完成"); // 通知加载成功
				listView.stopLoadMore(); // 开启LoadingMore功能

			}
			else if (msg.what==3){//删除完成
				refresh();
			}

		}
	};
	public void msgbox(String msg) 
	{
		new AlertDialog.Builder(this).setTitle("提示").setMessage(msg)
				.setNeutralButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_book);


		this.findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				String title=((EditText)findViewById(R.id.title)).getText().toString();
				String author=((EditText)findViewById(R.id.author)).getText().toString();
				String price=((EditText)findViewById(R.id.price)).getText().toString();
				String publisher=((EditText)findViewById(R.id.publisher)).getText().toString();
				if(title.equals("")||author.equals("")||price.equals("")||publisher.equals(""))
				{
					msgbox("不能为空");
					return;
				}
				thisbook=new Book();
				thisbook.setTitle(title);
				thisbook.setAuthor(author);
				thisbook.setPrice(Double.parseDouble(price));
				thisbook.setPublisher(publisher);
				new Thread(new Runnable(){
					@Override
					public void run() {
						sendHttpPost("http://"+ip+"/rest/addBook",thisbook);
					}}).start();
			}
		});

        listView = (ZrcListView) findViewById(R.id.lst_books);
        listView.setDividerHeight(-5);
        
        // 设置下拉刷新的样式（可选，但如果没有Header则无法下拉刷新）
        SimpleHeader header = new SimpleHeader(this);
        header.setTextColor(0xff0066aa);
        header.setCircleColor(0xff33bbee);
        listView.setHeadable(header);

        // 下拉刷新事件回调（可选）
        listView.setOnRefreshStartListener(new OnStartListener() {
            @Override
            public void onStart() {
                refresh();
            }
        });


        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);


		refresh(); // 主动下拉刷新
        
        
        
	}
            
    private void refresh(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					URL url = new URL("http://"+ip+"/rest/getBooks");
					HttpURLConnection mConnection = (HttpURLConnection) url.openConnection();
					mConnection.setConnectTimeout(10000);//设置连接超时

					//创建输入流
					InputStream in = null;
					//判断连接返回码
					if (mConnection.getResponseCode() == 200) {
						//返回码是200时，表明连接成功
						in = mConnection.getInputStream();
					}
					//创建输入流读取对象
					InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
					//输入流缓存读取
					BufferedReader bufferedReader = new BufferedReader(inReader);
					StringBuffer sb = new StringBuffer();
					String temp = null;
					//输入流转换成String
					while ((temp = bufferedReader.readLine()) != null) {
						sb.append(temp);
					}
					Gson gson=new Gson();
					String tmp=sb.toString();
					/*Type listType = new TypeToken<ArrayList<Book> >(){}.getType();
					books=gson.fromJson(tmp,listType);*/
					books=new ArrayList<Book>();
					JSONArray mres=new JSONArray(tmp);
					for (int i = 0; i < mres.length(); i++) {
						JSONObject thisBook = (JSONObject)mres.getJSONObject(i);
						Book booktmp=new Book();
						booktmp.setTitle(thisBook.getString("title"));
						booktmp.setAuthor(thisBook.getString("author"));
						booktmp.setPrice(Double.parseDouble(thisBook.getString("price")));
						booktmp.setPublisher(thisBook.getString("publisher"));
						booktmp.setId(Integer.parseInt(thisBook.getString("id")));
						books.add(booktmp);
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				//Handler创建消息对象
				Message msg = mHandler.obtainMessage();
				msg.what = 2;
				//发送Handler消息
				mHandler.sendMessage(msg);
			}
			}).start();

    }


    private class MyAdapter extends BaseAdapter{
    	private LayoutInflater layoutInflater = null;
    	public int iSelectPos=-1;
    	
        public MyAdapter(Context ctx) 
        {
        	layoutInflater=LayoutInflater.from(ctx);
		}

        @Override
        public int getCount() {
            return books.size();
        }
        @Override
        public Object getItem(int position) {
            return books.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	final BookItem_Holder item;
            if(convertView == null)
            {
            	item=new BookItem_Holder();
                convertView = layoutInflater.inflate(R.layout.listview, null);
                item.strTitle=(TextView)convertView.findViewById(R.id.title);
                item.strAuthor=(TextView)convertView.findViewById(R.id.author);
                item.strPrice=(TextView)convertView.findViewById(R.id.price);
				item.strPublisher=(TextView)convertView.findViewById(R.id.publisher);
                item.strID=(TextView)convertView.findViewById(R.id.sid);
                item.btnDel=(Button)convertView.findViewById(R.id.btn_del);
				item.btnEdit=(Button)convertView.findViewById(R.id.btn_edit);
                convertView.setTag(item);
            }
            else
            {
                item = (BookItem_Holder)convertView.getTag();
            }



            item.strTitle.setText(books.get(position).getTitle());
            item.strAuthor.setText(books.get(position).getAuthor());
            item.strPrice.setText(books.get(position).getPrice()+"");
			item.strPublisher.setText(books.get(position).getPublisher());
            item.strID.setText(books.get(position).getId()+"");
			item.position=position;
			item.btnEdit.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View v) {
				   thisbook=books.get(item.position);
				   Intent intent = new Intent();
				   intent.setClass(bookActivity.this, editBookActivity.class);
				   startActivity(intent);
			   }
		   });
            item.btnDel.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					new Thread(new Runnable(){
						@Override
						public void run() {
							try {
								URL url = new URL("http://"+ip+"/rest/deleteBook/"+item.strID.getText());
								HttpURLConnection mConnection = (HttpURLConnection) url.openConnection();
								mConnection.setConnectTimeout(10000);//设置连接超时

								//创建输入流
								InputStream in = null;
								//判断连接返回码
								if (mConnection.getResponseCode() == 200) {
									//返回码是200时，表明连接成功
									in = mConnection.getInputStream();
								}
								//创建输入流读取对象
								InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
								//输入流缓存读取
								BufferedReader bufferedReader = new BufferedReader(inReader);
								StringBuffer sb = new StringBuffer();
								String temp = null;
								//输入流转换成String
								while ((temp = bufferedReader.readLine()) != null) {
									sb.append(temp);
								}

							} catch (IOException e) {
								e.printStackTrace();
							}
							//Handler创建消息对象
							Message msg = mHandler.obtainMessage();
							msg.what = 3;
							//发送Handler消息
							mHandler.sendMessage(msg);
						}
					}).start();

				}
			});
            
            
            return convertView;
            
        }
        
    }
    static class BookItem_Holder {
    	public TextView strID;
        public TextView strTitle;
        public TextView strAuthor;
        public TextView strPrice;
		public TextView strPublisher;
        public Button btnDel;
		public Button btnEdit;
		public int position;
    }


}
