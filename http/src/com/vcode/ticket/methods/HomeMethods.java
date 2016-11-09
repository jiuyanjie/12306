package com.vcode.ticket.methods;

import java.util.Date;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vcode.http.client.VHttpResponse;
import com.vcode.http.client.methods.VHttpGet;
import com.vcode.http.client.methods.VHttpPost;
import com.vcode.http.client.parames.VParames;
import com.vcode.http.utils.VBrowser;
import com.vcode.http.utils.VHttpUtils;
import com.vcode.ticket.ui.Home_Page;
import com.vcode.ticket.utils.HttpUtils;

/**
 * 提交订单类
 * @author Administrator
 *
 */
public class HomeMethods extends Thread {
	
	public Home_Page home_page;
	
	private JSONObject obj2;
	
	private HomeMethods(){};
	
	private String passengerTicketStr;
	
	public HomeMethods(Home_Page home_page){
		if (this.home_page==null) {
			this.home_page = home_page;
		}
	};
	
	public HomeMethods(Home_Page home_page,JSONObject obj){
		if (this.home_page==null) {
			this.home_page = home_page;
		}
		if (this.obj2==null) {
			this.obj2 = obj;
		}
	}

	@Override
	public void run() {
		SubmitOrder();
	}

	/**
	 * 获取乘客列表
	 * 
	 * @return
	 */
	private DefaultListModel<Object> getPassengerDTOs() {
		DefaultListModel<Object> model_Seats = new DefaultListModel<Object>();
		VHttpPost post = new VHttpPost(
				"https://kyfw.12306.cn/otn/confirmPassenger/getPassengerDTOs");
		VHttpResponse res = VBrowser.execute(post);
		String body = VHttpUtils.outHtml(res.getBody());
		try {
			JSONObject res_obj = new JSONObject(body);
			JSONObject userListObj = (JSONObject) res_obj.get("data");
			if (userListObj.length() < 1) {
				home_page.textArea.append(home_page.format.format(new Date()) + "："
						+ new JSONObject(body).get("messages") + "\r\n");
			} else {
				JSONArray userArr = (JSONArray) (userListObj
						.get("normal_passengers"));
				for (int i = 0; i < userArr.length(); i++) {
					JSONObject obj = (JSONObject) userArr.get(i);
					model_Seats.addElement(obj.get("passenger_name"));
					home_page.userMap.put(obj.get("passenger_name").toString(), obj);
				}
			}
		} catch (JSONException e) {
			home_page.textArea.append(home_page.format.format(new Date())
					+ "：获取乘客列表失败，请联系作者QQ：3094759846\r\n");
		}
		return model_Seats;
	}

	/**
	 * 查询订单列表
	 */
	private void getOrderList(JButton btnNewButton_1) {

		try {
			VHttpPost post = new VHttpPost(
					"https://kyfw.12306.cn/otn/queryOrder/queryMyOrderNoComplete");
			VParames parames = new VParames();
			parames.clear();
			parames.put("_json_att", "");
			post.setParames(parames);
			VHttpResponse res = VBrowser.execute(post);
			String body = VHttpUtils.outHtml(res.getBody());
			JSONObject res_obj = new JSONObject(body);
			disposeOrder(res_obj, btnNewButton_1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理订单列表
	 */
	private void disposeOrder(JSONObject res_obj, JButton btnNewButton_1) {
		try {
			home_page.table_1.setModel(new DefaultTableModel(new Object[][] {},
					new String[] { "\u8F66\u6B21", "\u8BA2\u5355\u53F7",
							"\u4E58\u5BA2\u59D3\u540D",
							"\u53D1\u8F66\u65F6\u95F4", "\u51FA\u53D1\u5730",
							"\u76EE\u7684\u5730", "\u7968\u79CD",
							"\u5E2D\u522B", "\u8F66\u53A2", "\u5EA7\u4F4D",
							"\u7968\u4EF7", "\u72B6\u6001" }) {
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			home_page.table_1.getColumnModel().getColumn(3).setPreferredWidth(124);
			DefaultTableModel model = (DefaultTableModel) home_page.table_1.getModel();
			if (btnNewButton_1 != null) {
				btnNewButton_1.setEnabled(true);
			}
			if (res_obj.isNull("data")) {
				model.setRowCount(0);
				return;
			}
			JSONObject userListObj = (JSONObject) res_obj.get("data");

			JSONArray jsonArr = ((JSONArray) userListObj.get("orderDBList"));

			for (int i = 0; i < jsonArr.length(); i++) {
				JSONObject obj = jsonArr.getJSONObject(i);
				JSONArray jsonArr2 = ((JSONArray) obj.get("tickets"));
				JSONObject tickets = jsonArr2.getJSONObject(0);
				JSONObject stationTrainDTO = (JSONObject) tickets
						.get("stationTrainDTO");
				JSONObject passengerDTO = (JSONObject) tickets
						.get("passengerDTO");

				Vector<String> vector = new Vector<String>();
				vector.add(obj.get("train_code_page").toString());
				vector.add(obj.get("sequence_no").toString());
				vector.add(passengerDTO.get("passenger_name").toString());
				vector.add(obj.get("start_train_date_page").toString());
				vector.add(stationTrainDTO.get("from_station_name").toString());
				vector.add(stationTrainDTO.get("to_station_name").toString());
				vector.add(tickets.get("ticket_type_name").toString());
				vector.add(tickets.get("seat_type_name").toString());
				vector.add(tickets.get("coach_name").toString());
				vector.add(tickets.get("seat_name").toString());
				vector.add(tickets.get("str_ticket_price_page").toString());
				vector.add(tickets.get("ticket_status_name").toString());
				model.addRow(vector);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 取消订单
	 * @param orderId
	 * @param button_3
	 */
	private void cancelOrder(String orderId, JButton button_3) {
		try {
			VHttpPost post = new VHttpPost(
					"https://kyfw.12306.cn/otn/queryOrder/cancelNoCompleteMyOrder");
			VParames parames = new VParames();
			parames.clear();
			parames.put("_json_att", "");
			parames.put("cancel_flag", "cancel_order");
			parames.put("sequence_no", orderId);
			post.setParames(parames);
			VHttpResponse res = VBrowser.execute(post);
			String body = VHttpUtils.outHtml(res.getBody());
			JSONObject res_obj = new JSONObject(body);
			JSONObject data_obj = (JSONObject) res_obj.get("data");
			if ("N".equals(data_obj.get("existError"))) {
				home_page.textArea.append(home_page.format.format(new Date()) + "：订单取消成功\r\n");
			}
			if (button_3 != null) {
				button_3.setEnabled(true);
			}
			Thread.sleep(2000);
			getOrderList(null);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 提交订单
	 */
	private void SubmitOrder() {
		String username = home_page.list_3.getModel().getElementAt(0).toString();
		JSONObject userObj = home_page.userMap.get(username);
		if (obj2==null) {
			obj2 = home_page.datalist.get(home_page.table.getSelectedRow());
		}
		DefaultListModel<Object> model = (DefaultListModel<Object>)home_page.list_2.getModel();
		String[] seatTypes = new String[model.getSize()];
		for (int i=0;i<model.getSize();i++) {
			seatTypes[i] = model.get(i).toString();
		}
		String station_train_code = "";
		try {
			station_train_code = obj2.get("station_train_code").toString();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		passengerTicketStr = HttpUtils.getPassengerTicketStr(userObj, seatTypes, station_train_code);
		if (obj2==null) {
			obj2 = home_page.datalist.get(home_page.table.getSelectedRow());
		}
		try {
			// 预订车票
			home_page.printLog("订单线程已启动，开始提交订票信息");
			VHttpPost post = new VHttpPost(
					"https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest");
			VParames parames = new VParames();
			parames.clear();
			parames.put("secretStr", obj2.get("secretStr").toString());
			parames.put("train_date", home_page.textField_2.getText());
			parames.put("back_train_date", home_page.textField_2.getText());
			parames.put("tour_flag", "dc");
			parames.put("purpose_codes", "ADULT");
			parames.put("query_from_station_name", obj2.get("from_station_name")
					.toString());
			parames.put("query_to_station_name", obj2.get("to_station_name")
					.toString());
			parames.put("undefined", "");
			post.setParames(parames);
			VHttpResponse res = VBrowser.execute(post);
			String body = VHttpUtils.outHtml(res.getBody());
			JSONObject res_obj = new JSONObject(body);
			if ("true".equals(res_obj.get("status").toString())) {
				home_page.printLog("订票信息提交成功");
				res.getEntity().disconnect();
				initDc();
			} else {
				home_page.printLog(res_obj.get("messages").toString());
				res.getEntity().disconnect();
			}
		} catch (JSONException e) {
			home_page.printLog("提交订单失败，请联系作者QQ：3094759846");
		}
	}

	/**
	 * 预定界面
	 */
	private void initDc() {
		VHttpPost post = new VHttpPost(
				"https://kyfw.12306.cn/otn/confirmPassenger/initDc");
		VParames parames = new VParames();
		parames.clear();
		parames.put("_json_att", "");
		post.setParames(parames);
		VHttpResponse res = VBrowser.execute(post);
		String body = VHttpUtils.outHtml(res.getBody());

		Pattern pattern = Pattern
				.compile("var globalRepeatSubmitToken = '[0-9 | a-z]{32}'");
		Pattern pattern2 = Pattern
				.compile("'key_check_isChange':'[0-9 | A-Z]{56}'");
		Matcher matcher = pattern.matcher(body);
		Matcher matcher2 = pattern2.matcher(body);
		while (matcher.find()) {
			home_page.REPEAT_SUBMIT_TOKEN = matcher.group(0)
					.replace("var globalRepeatSubmitToken = '", "")
					.replace("'", "");
		}
		while (matcher2.find()) {
			home_page.key_check_isChange = matcher2.group(0)
					.replace("'key_check_isChange':'", "").replace("'", "");
		}
		res.getEntity().disconnect();
		home_page.printLog("开始拉取验证......");
		getSubmitCode();
	}

	/**
	 * 拉取提交订单验证码及校验，返回true表示校验成功，反之否
	 * 
	 * @return 校验是否成功
	 */
	public void getSubmitCode() {
		// 拉取验证码
		String url = "https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew?module=passenger&rand=randp&"
				+ Math.random();
		VHttpGet get = new VHttpGet(url);
		VHttpResponse res = VBrowser.execute(get); // 获取验证码
		HttpUtils.getSubmitCodeBy12306(res.getBody(), this);
		res.getEntity().disconnect(); // 耗尽资源
	}

	/**
	 * 
	 * 校验验证码是否正确
	 * 
	 */
	public void checkSubmitCode() {
		String code = HttpUtils.incode.toString().substring(0,
				HttpUtils.incode.toString().length() - 1);
		home_page.printLog("当前验证码：" + code);
		VHttpPost post = new VHttpPost(
				"https://kyfw.12306.cn/otn/passcodeNew/checkRandCodeAnsyn");
		VParames parames5 = new VParames();
		parames5.put("randCode", code);
		parames5.put("rand", "randp");
		parames5.put("_json_att", "");
		parames5.put("REPEAT_SUBMIT_TOKEN", home_page.REPEAT_SUBMIT_TOKEN);

		post.setParames(parames5);
		VHttpResponse res = VBrowser.execute(post);
		String body = VHttpUtils.outHtml(res.getBody());
		try {
			JSONObject res_obj = new JSONObject(body);
			JSONObject dataObj = (JSONObject) res_obj.get("data");
			if ("1".equals(dataObj.get("result").toString())) {
				home_page.printLog("验证码正确，开始确认用户是否可以提交订单");
				checkOrderInfo();
			} else {
				home_page.printLog("验证码错误，请重新验证");
				getSubmitCode();
			}
		} catch (JSONException e) {
			home_page.printLog("解析验证码错误，请联系作者QQ：3094759846");
		}
	}

	/**
	 * 确认用户是否可以提交订单
	 */
	private void checkOrderInfo() {
		String username = home_page.list_3.getModel().getElementAt(0).toString();
		JSONObject userObj = home_page.userMap.get(username);
		try {
			VHttpPost post = new VHttpPost(
					"https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo");
			VParames parames = new VParames();
			parames.clear();
			parames.put("cancel_flag", "2");
			parames.put("bed_level_order_num", "000000000000000000000000000000");
			parames.put("passengerTicketStr",passengerTicketStr);
			parames.put("oldPassengerStr", userObj.getString("passenger_name")
					+ ",1," + userObj.getString("passenger_id_no") + ",1_");
			parames.put("tour_flag", "dc");
			parames.put(
					"randCode",
					HttpUtils.incode.toString().substring(0,
							HttpUtils.incode.toString().length() - 1));
			parames.put("_json_att", "");
			parames.put("REPEAT_SUBMIT_TOKEN", home_page.REPEAT_SUBMIT_TOKEN);
			post.setParames(parames);
			VHttpResponse res = VBrowser.execute(post);
			String body = VHttpUtils.outHtml(res.getBody());
			JSONObject res_obj = new JSONObject(body);
			JSONObject dataobj = new JSONObject(res_obj.get("data").toString());
			if ("true".equals(dataobj.get("submitStatus").toString())) {
				home_page.printLog("当前用户可以提交订单");
				getQueueCount();
			} else {
				home_page.printLog(dataobj.get("errMsg").toString());
				return;
			}
			res.getEntity().disconnect();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取余票数量
	 */
	private void getQueueCount() {
		if (obj2==null) {
			obj2 = home_page.datalist.get(home_page.table.getSelectedRow());
		}
		VHttpPost post = new VHttpPost(
				"https://kyfw.12306.cn/otn/confirmPassenger/getQueueCount");
		VParames parames4 = new VParames();
		try {
			parames4.put("train_date", home_page.format2.parse(home_page.textField_2.getText())
					+ "");
			parames4.put("train_no", obj2.get("train_no").toString());
			parames4.put("stationTrainCode", obj2.get("station_train_code")
					.toString());
			parames4.put("seatType", "3");
			parames4.put("fromStationTelecode", obj2
					.get("from_station_telecode").toString());
			parames4.put("toStationTelecode", obj2.get("to_station_telecode")
					.toString());
			parames4.put("leftTicket", obj2.getString("yp_info"));
			parames4.put("purpose_codes", "00");
			parames4.put("train_location", obj2.getString("location_code"));
			parames4.put("_json_att", "");
			parames4.put("REPEAT_SUBMIT_TOKEN", home_page.REPEAT_SUBMIT_TOKEN);
			post.setParames(parames4);
			VHttpResponse res = VBrowser.execute(post);
			String body = VHttpUtils.outHtml(res.getBody());
			JSONObject jsonBody = new JSONObject(body);
			if ("true".equals(jsonBody.get("status").toString())) {
				JSONObject dataObj = (JSONObject) jsonBody.get("data");
				String[] counts = HttpUtils.getCountByJs(
						dataObj.get("ticket").toString(), passengerTicketStr.substring(0, 1)).split(",");
				if (Integer.parseInt(counts[0]) > 0) {
					home_page.printLog(obj2.get("station_train_code") + "："+HttpUtils.seatNumToseatType(passengerTicketStr.substring(0, 1))+"剩余:"
							+ counts[0] + "张");
				}
				home_page.printLog("开始提交订单");
			} else {
				home_page.printLog(jsonBody.get("messages").toString());
			}
			res.getEntity().disconnect();
		} catch (Exception e) {
			home_page.printLog("解析余票数量失败，请联系作者QQ：3094759846");
		}
		confirmSingleForQueue();
	}

	/**
	 * 确认提交订单
	 */
	private void confirmSingleForQueue() {
		String username = home_page.list_3.getModel().getElementAt(0).toString();
		JSONObject userObj = home_page.userMap.get(username);
		if (obj2==null) {
			obj2 = home_page.datalist.get(home_page.table.getSelectedRow());
		}

		try {
			VHttpPost post = new VHttpPost(
					"https://kyfw.12306.cn/otn/confirmPassenger/confirmSingleForQueue");
			VParames parames = new VParames();
			parames.clear();
			parames.put("passengerTicketStr",passengerTicketStr);
			parames.put("oldPassengerStr", userObj.getString("passenger_name")
					+ ",1," + userObj.getString("passenger_id_no") + ",1_");
			parames.put(
					"randCode",
					HttpUtils.incode.toString().substring(0,
							HttpUtils.incode.toString().length() - 1));
			parames.put("purpose_codes", "00");
			parames.put("key_check_isChange", home_page.key_check_isChange);
			parames.put("leftTicketStr", obj2.getString("yp_info"));
			parames.put("train_location", obj2.getString("location_code"));
			parames.put("roomType", "00");
			parames.put("dwAll", "N");
			parames.put("_json_att", "");
			parames.put("REPEAT_SUBMIT_TOKEN", home_page.REPEAT_SUBMIT_TOKEN);
			post.setParames(parames);
			VHttpResponse res = VBrowser.execute(post);
			String body = VHttpUtils.outHtml(res.getBody());
			JSONObject res_obj = new JSONObject(body);
			JSONObject dataobj = new JSONObject(res_obj.get("data").toString());
			if ("true".equals(dataobj.get("submitStatus").toString())) {
				home_page.printLog("订单提交成功，正在查询订票结果");
				queryOrderWaitTime();
			} else {
				home_page.printLog(body);
			}
			res.getEntity().disconnect();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开始查询订单
	 */
	private void queryOrderWaitTime() {
		if (obj2==null) {
			obj2 = home_page.datalist.get(home_page.table.getSelectedRow());
		}
		boolean order = true;
		String orderId = "";
		try {
			while (order) {
				Random ne = new Random();
				int x = ne.nextInt(9999 - 1000 + 1) + 1000;
				String query_url = "https://kyfw.12306.cn/otn/confirmPassenger/queryOrderWaitTime?";
				query_url = query_url + "random=14772940" + x
						+ "&tourFlag=dc&_json_att=&REPEAT_SUBMIT_TOKEN="
						+ home_page.REPEAT_SUBMIT_TOKEN;
				VHttpGet get = new VHttpGet(query_url);
				VHttpResponse res = VBrowser.execute(get);
				String body = VHttpUtils.outHtml(res.getBody());
				JSONObject res_obj = new JSONObject(body);
				JSONObject dataobj = new JSONObject(res_obj.get("data")
						.toString());
				if (!"null".equals(dataobj.get("orderId").toString())) {
					order = false;
					orderId = dataobj.get("orderId").toString();
				}
			}
			home_page.printLog("恭喜你，成功订到一张"
					+ obj2.getString("from_station_name") + "至"
					+ obj2.getString("end_station_name") + "的"+HttpUtils.seatNumToseatType(passengerTicketStr.substring(0, 1))+"，订单号为：" + orderId
					+ "，请尽快付款，以免耽误行程");
		} catch (JSONException e) {
			home_page.printLog("：解析订票结果失败，请联系作者QQ：3094759846");
		}
	}

}