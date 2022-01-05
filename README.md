# 从 A 网域重导向到 B 网域

---

## 缘起

自从原网域 ddfans.club 更换到新网域 ddfans.app 后，在 GoDaddy 设定了转址…

![](https://i.imgur.com/ESVLvso.png)

但不知是原先在 CloudFlare 的 DNS 设定还未完整 propagate 到整个互联网，还是因为 AWS Certificate Manager 的因素，反正 ddfans.club 就是无法重导向到 ddfans.app；期间也尝试过了用 Amazon Route 53、AWS CloudFront、Amazon S3 的方式依然无果。万念俱灰之下只好写 webapp 了。

## 需求(规格)

- 将任何 HTTP GET 请求重导至另一网域。
- 重导的网域别写死。
- 所有 query parameter(s) 也要重导过去。
- 取得来源网域并带入 `utm_source` 一起重导过去。

## 剖析请求参数

要注意的事项：

- 可能没有任何参数。
- 某些参数键可能会有多个值(重复键)。
- 最后带入 utm 参数。

```java
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
```

### 行 30 ~ 35

若无参数则直接带入 `utm_source` 并返回。

### 行 40 ~ 56

逐键剖析其各对应的值。

#### 行 48

若为重复键则在字符串结尾加入 `&` 字符。

#### 行 51 ~ 54

若仍有其它键则在字符串结尾加入 `&` 字符。

### 行 58 ~ 65

将 36 ~ 56 的结果字符串带入 `utm_source` 并返回。

## 任何 HTTP GET 请求

```java
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
```

### 行 81

重导向的网域，从系统环境变量取得。

### 行 82

原请求的网址。

### 行 83 ~ 86

剖析请求参数。

## 结论

- 奉行三民主义❗️
- 服从政府领导❓
- 保卫国家安全⁉️
- 完成统一大业‼ ️
