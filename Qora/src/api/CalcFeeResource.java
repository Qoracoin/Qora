package api;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.crypto.Base58;
import utils.Pair;
import controller.Controller;
import api.BlogPostResource;

@Path("calcfee")
@Produces(MediaType.APPLICATION_JSON)
public class CalcFeeResource {

	@SuppressWarnings("unchecked")
	@POST
	@Path("/arbitrarytransactions")
	@Consumes(MediaType.WILDCARD)
	public String calcFeeForArbitraryTransaction(String x)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String data = (String) jsonObject.get("data");
			
			//PARSE DATA
			byte[] dataBytes;
			try
			{
				dataBytes = Base58.decode(data);
			}
			catch(Exception e)
			{
				throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_DATA);
			}
				
			jsonObject = new JSONObject();
			
			//CALC FEE
			Pair<BigDecimal, Integer> result = Controller.getInstance().calcRecommendedFeeForArbitraryTransaction(dataBytes, null);
			
			jsonObject.put("fee", result.getA().toPlainString());
			jsonObject.put("feeRound", result.getA().setScale(0, BigDecimal.ROUND_CEILING).setScale(8).toPlainString());
			jsonObject.put("length", result.getB());
			return jsonObject.toJSONString();
			
		}
		catch(NullPointerException e)
		{
			//JSON EXCEPTION
			//e.printStackTrace();
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			//e.printStackTrace();
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/namereg")
	@Consumes(MediaType.WILDCARD)
	public String calcFeeForNameReg(String x) {
		try {
			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String name = (String) jsonObject.get("name");
			String value = (String) jsonObject.get("value");

			jsonObject = new JSONObject();
			Pair<BigDecimal, Integer> result = Controller.getInstance().calcRecommendedFeeForNameRegistration(name, value); 
			jsonObject.put("fee", result.getA().toPlainString());
			jsonObject.put("feeRound", result.getA().setScale(0, BigDecimal.ROUND_CEILING).setScale(8).toPlainString());
			jsonObject.put("length", result.getB());
			return jsonObject.toJSONString();
			
		} catch (NullPointerException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		} catch (ClassCastException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		}
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/nameupdate")
	@Consumes(MediaType.WILDCARD)
	public String calcFeeForNameUpdate(String x) {
		try {
			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String name = (String) jsonObject.get("name");
			String value = (String) jsonObject.get("newvalue");

			jsonObject = new JSONObject();
			Pair<BigDecimal, Integer> result = Controller.getInstance().calcRecommendedFeeForNameUpdate(name, value); 
			jsonObject.put("fee", result.getA().toPlainString());
			jsonObject.put("feeRound", result.getA().setScale(0, BigDecimal.ROUND_CEILING).setScale(8).toPlainString());
			jsonObject.put("length", result.getB());
			return jsonObject.toJSONString();
			
		} catch (NullPointerException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		} catch (ClassCastException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		}
	}

	
	
	
//	TODO CALC FREE FOR COMMENTPOST ALSO NEEDED
	@SuppressWarnings("unchecked")
	@POST
	@Path("/blogpost")
	public String calcFeeForBlogPost(String x) {
		try {

			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			
			String blogname = (String) jsonObject.get("blogname");
			String authorOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
			String title = (String) jsonObject.get("title");
			String body = (String) jsonObject.get("body");

			if (StringUtil.isBlank(body)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_BODY_EMPTY);
			}

			JSONObject dataStructure = new JSONObject();

			dataStructure.put(BlogPostResource.TITLE_KEY, title);
			dataStructure.put(BlogPostResource.POST_KEY, body);

			if (blogname != null) {
				dataStructure.put(BlogPostResource.BLOGNAME_KEY, blogname);
			}

			if (authorOpt != null) {
				dataStructure.put(BlogPostResource.AUTHOR, authorOpt);
			}

			jsonObject = new JSONObject();
			
			//CALC FEE
			Pair<BigDecimal, Integer> result = Controller.getInstance().calcRecommendedFeeForArbitraryTransaction(dataStructure.toJSONString().getBytes(StandardCharsets.UTF_8), null);
			
			jsonObject.put("fee", result.getA().toPlainString());
			jsonObject.put("feeRound", result.getA().setScale(0, BigDecimal.ROUND_CEILING).setScale(8).toPlainString());
			jsonObject.put("length", result.getB());
			return jsonObject.toJSONString();

		} catch (NullPointerException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		} catch (ClassCastException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		}

	}
	
}

