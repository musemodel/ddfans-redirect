package tw.musemodel.jkfans.redirect;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 根控制器
 *
 * @author p@musemodel.tw
 */
@RestController
public class OneAndOnlyController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OneAndOnlyController.class);

	private static final String REDIRECT_HOST = System.getenv("REDIRECT_HOST");

	/**
	 * @param parameters 查询参数
	 * @param serverName
	 * @return 查询参数字符串
	 */
	private static String queryString(
		final Map<String, String[]> parameters,
		final String serverName
	) {
		if (parameters.isEmpty()) {
			return String.format(
				"?utm_source=%s",
				serverName
			);
		}
		StringBuilder stringBuilder = new StringBuilder("?");

		int numberOfKeys = parameters.size();
		int orderOfKeys = 0;
		for (String key : parameters.keySet()) {
			String[] values = parameters.get(key);
			int numberOfValues = values.length;
			for (int i = 0; i < numberOfValues; ++i) {
				stringBuilder.append(String.format(
					"%s=%s",
					key,
					values[i]
				)).append(i < numberOfValues - 1 ? "&" : "");
			}

			if (orderOfKeys < numberOfKeys - 1) {
				stringBuilder.
					append("&");
			}
			++orderOfKeys;
		}

		return stringBuilder.
			append(
				String.format(
					"&utm_source=%s",
					serverName
				)
			).
			toString();
	}

	/**
	 * @param request 任何 GET 请求
	 * @return 重导向
	 */
	@GetMapping(path = "/**")
	ModelAndView index(HttpServletRequest request) {
		return new ModelAndView(
			String.format(
				"redirect:https://%s%s%s",
				REDIRECT_HOST,
				request.getRequestURI(),
				queryString(
					request.getParameterMap(),
					request.getServerName()
				)
			)
		);
	}
}
