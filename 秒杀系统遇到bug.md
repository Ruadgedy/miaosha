### 第一章 项目环境搭建

缓存采用Redis + Jedis + 通用缓存Key封装

### 第二章实现登录功能

1. 数据库设计

   > 核心数据库有： user、goods、miaosha_goods、miaosha_user
   >
   > 后面还添加了专门用于保存秒杀信息的miaosha_order  ,  order_info

2. 明文密码两次MD5处理

   1. 用户端：pass=MD5(明文+固定salt) ： 防止用户密码在网络上明文传输
   2. 服务端：pass=MS5(用户输入+随机salt)： 防止数据库泄露，敌手反推出原密码

   > 首先在登录页面获取到用户密码的时候加盐并进行MD5处理，在将处理结果传到后端接口的之后，再次加盐并用MD5处理。

3. JSR303参数校验+全局异常处理器

   > 对于输入的电话号码，采用自定义参数校验规则。定义IsMobile注解以及IsMobileValidator校验规则
   >
   > 全局异常处理器定义如下：
   >
   > ```java
   > public class GlobalException extends RuntimeException {
   > 
   > 	private CodeMsg cm;
   > 
   > 	public CodeMsg getCm() {
   > 		return cm;
   > 	}
   > 
   > 	public GlobalException(CodeMsg cm){
   > 		super(cm.toString());
   > 		this.cm = cm;
   > 	}
   > }
   > ```
   >
   > CodeMsg定义全局返回状态吗和返回信息。方便前后端分离

   4. 分布式session

      分布式Session：不是将session存入到服务器当中，而是将session存入到redis缓存中。用redis单独管理缓存

   > 存在的小问题：服务器中的session缓存在每次访问时候都会重新刷新有效期，而我们实现的redis缓存是将过期时间写死了
   >
   > 改进：在每次获取session token时，延长session有效期（重新设置缓存有效期）

   > 采用Jedis手动实现。
   >
   > ```java
   > public interface KeyPrefix {
   > 
   > 	public int expireSeconds();
   > 
   > 	public String getPrefix();
   > }
   > ```
   >
   > ```java
   > public abstract class BasePrefix implements KeyPrefix {
   > 	private int expireSeconds; // 过期时间
   > 	private String prefix;  // 前缀
   > 
   > 	// 0代表永不过期
   > 	public BasePrefix(String prefix){
   > 		this(0, prefix);
   > 	}
   > 
   > 	public BasePrefix(int expireSeconds, String prefix){
   > 		this.expireSeconds = expireSeconds;
   > 		this.prefix = prefix;
   > 	}
   > 
   > 	@Override
   > 	public int expireSeconds() {
   > 		return expireSeconds;
   > 	}
   > 
   > 	/**
   > 	 * 为了区分各个板块，前缀采用   类名+prefix  的方式
   > 	 * @return
   > 	 */
   > 	@Override
   > 	public String getPrefix() {
   > 		return getClass().getSimpleName() + ":" + prefix;
   > 	}
   > }
   > ```
   >
   > 继承BasePrefix就可以实现自定义前缀和过期时间的key

```java
// 低配版，在每次请求到来的时候都要先去取cookie，然后从redis中拿到用户token
// 会产生大量的代码冗余
@RequestMapping("/to_list")
	public String list(HttpServletResponse response, Model model,
	                   @CookieValue(value = MiaoshaUserService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
	                   @RequestParam(value = MiaoshaUserService.COOKIE_NAME_TOKEN,required = false)String paramToken) {
		if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){ // 都为空，则说明用户未登录，先去登录
			return "login";
		}
		String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
		MiaoshaUser user = userService.getByToken(response, token);
		model.addAttribute("user",user);
		return "goods_list";
	}
```

为了解决代码冗余问题，增加参数解析功能：

```java
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

	@Autowired
	MiaoshaUserService userService;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == MiaoshaUser.class;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

		String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
		String cookieToken = getCookieValue(request,MiaoshaUserService.COOKIE_NAME_TOKEN);
		if (StringUtils.isEmpty(paramToken) && StringUtils.isEmpty(cookieToken)){
			return null;
		}
		String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
		return userService.getByToken(response,token);
	}

	private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies){
			if (cookie.getName().equals(cookieNameToken)){
				return cookie.getValue();
			}
		}
		return null;
	}
}

```

经过改造，上面的接口就变为：

```java
@RequestMapping("/to_list")
	public String list(Model model, MiaoshaUser user){
		model.addAttribute("user",user);
		return "goods_list";
	}
```



### 第三章 实现秒杀功能

1. 数据库设计
2. 商品列表页
3. 商品详情页
4. 订单详情页

> 问题1：在Jmeter压测下，商品库存会出现负数。也就是出现了超卖现象
>
> ```java
> // 这里涉及到高并发下的一致性问题
> 	// 当启动1w个线程时，会出现库存为负的情况
> 	// 出现该问题的原因也很简单，当多个线程同时跑到判断商品库存的代码处时，会有多个线程判断库存==1，然后执行减库存的操作
> 	@RequestMapping(value = "/do_miaosha",method = RequestMethod.POST)
> 	@ResponseBody
> 	public Result<OrderInfo> list(Model model, MiaoshaUser user, @RequestParam("goodsId") long goodsId){
> 		model.addAttribute("user",user);
> 		if (user == null) return Result.error(CodeMsg.SESSION_ERROR);
> 		// 判断商品库存
> 		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
> 		int stock = goods.getStockCount();
> 		if (stock <= 0){ // 库存小于0，则秒杀结束
> 			return Result.error(CodeMsg.MIAO_SHA_OVER);
> 		}
> 		// 判断是否秒杀到了
> 		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
> 		if (order != null) {  // 已经存在秒杀订单信息，则说明重复秒杀
> 			return Result.error(CodeMsg.REPEAT_MIAOSHA);
> 		}
> 		// 做秒杀业务（事务）
> 		// 1. 减库存  2. 下订单  3. 写如秒杀订单
> 		OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
> 		return Result.success(orderInfo);
> 	}
> ```
>
> 本质原因是：数据库语句设计有问题
>
> 数据库本身会对记录加锁，不会出现多个线程同时更新一个线程的情况
>
> ```
> // 第一句sql会有并发问题。假设库存只剩一个了，此时有多个线程到来访问。则会同时将库存减一，减为负数
> // @Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId}  ")
>    // 下面的sql会在减库存时再执行库存检查
>    @Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId} and stock_count > 0")
>    public int reduceStock(MiaoshaGoods g);
> ```
>
>  
>
> 问题2：
>
> ```java
> GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
> 在执行上面的语句时，加入有用户同时发出两条请求，恰好这两个请求都被认为是秒杀到了。这时会出现一个用户秒杀多个商品的情况。这是不被允许的
> ```
>
> 解决：在项目中会用到  “秒杀订单”这个表。我们限制一个用户只能生成一个秒杀订单，即一个用户只能秒杀一个商品。所以解决方法是在“秒杀订单表”上建立唯一索引，第一个订单可以被插入进来，但是同一用户的第二个订单就插入不进来了，插入不进来了就会报错，报错@Transactional就会回滚
>
> 实际的项目中我们会让用户在秒杀的时候输入验证码，这可以防止 用户同时发出两个请求
>
>  
>
> 优化：
>
> 在判断用户是否重复下单时，我们会去“秒杀订单表”中查找用户记录，所以优化过程就是将用户下单成功的信息保存如缓存中：下次查找时，取缓存中的数据即可
>
> ```java
> // 下单成功后会将秒杀订单写入到缓存中。这样当判断用户是否重复下单时就不需要去数据库中寻找
> 		redisService.set(OrderKey.getMiaoshaOrderByUidGid,"" + user.getId() + "_" + goods.getId(), miaoshaOrder);
> ```
>
> > 超卖问题：
> >
> > 解决超卖：1.数据库加唯一索引：防止用户重复购买
> >
> > 2.SQL加库存数量判断：防止库存变为负数



### 第四章 JMeter压测

### 第五章 页面优化技术

大并发下的瓶颈是数据库。所以要提高高并发下的数据访问能力，需要加大量的缓存

1. 页面缓存+URL缓存+对象缓存
2. 页面静态化，前后端分离
3. 静态资源优化
4. CDN优化

> 页面缓存：
>
> 访问页面的时候不是直接让系统去渲染，而是先从缓存当中去取。找到了就返回给客户端。如果没有，则手动渲染模板，将渲染结果输出给客户端，同时将渲染结果缓存到redis
>
> ```java
> /**
> 	 * 页面缓存优化：
> 	 * 初始版本是将用户数据绑定到Model，传递到页面上
> 	 *
> 	 * 新增produces="text/html"
> 	 * 也就是说改进后是直接返回网页的源代码
> 	 */
> 	@RequestMapping(value = "/to_list", produces = "text/html")
> 	@ResponseBody  // 手动渲染的时候，必须使用@ResponseBody，不能单纯使用@Controller
> 	public String list(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user){
> 		model.addAttribute("user",user);
> 
> 		// 首先从缓存中取
> 		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
> 		if (!StringUtils.isEmpty(html)){
> 			// 不为空直接返回页面
> 			return html;
> 		}
> 
> 		// 查询商品列表
> 		List<GoodsVo> goodsList = goodsService.listGoodsVo();
> 		model.addAttribute("goodsList",goodsList);
> 
> 		// 缓存中娶不到，手动渲染
> 		SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
> 		html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
> 		// 手动渲染完成后，将html结果保存在缓存中去
> 		if (!StringUtils.isEmpty(html)){
> 			redisService.set(GoodsKey.getGoodsList,"", html);
> 		}
> 		return html;
> 	}
> 
> 上面的代码属于粗粒度缓存，用户可能看到的是5秒之前的页面
> ```
>
> URL缓存：
>
> 用在商品详情页。不同的页面有不同的详情，所以是有区分的，称为URL缓存
>
> ```java
> @RequestMapping(value = "/to_detail/{goodsId}",produces = "text/html")
> 	@ResponseBody  // 手动渲染必须加上@ResponseBody注解
> 	public String detail(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user, @PathVariable("goodsId") long goodsId){
> 		model.addAttribute("user",user);
> 
> 		// 取缓存
> 		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
> 		if(!StringUtils.isEmpty(html)){
> 			return html;
> 		}
> 
> 		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
> 		model.addAttribute("goods", goods);
> 
> 		long startAt = goods.getStartDate().getTime();
> 		long endAt = goods.getEndDate().getTime();
> 		long now = System.currentTimeMillis();
> 
> 		int miaoshaStatus = 0;
> 		int remainSeconds = 0;
> 
> 		if (now < startAt){ // 秒杀没开始，倒计时
> 			miaoshaStatus = 0;
> 			remainSeconds = (int) ((startAt - now) / 1000);
> 		}else if (now > endAt){ // 秒杀已经结束
> 			miaoshaStatus = 2;
> 			remainSeconds = -1;
> 		}else { // 秒杀正在进行中
> 			miaoshaStatus = 1;
> 			remainSeconds = 0;
> 		}
> 
> 		model.addAttribute("miaoshaStatus", miaoshaStatus);
> 		model.addAttribute("remainSeconds", remainSeconds);
> 
> 		// 手动渲染
> 		SpringWebContext context = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
> 		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",context);
> 		if (!StringUtils.isEmpty(html)){
> 			redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
> 		}
> 		return html;
> 	}
> ```
>
> 对象缓存：更细粒度的缓存。根据对象ID去取缓存
>
> ```java
> public MiaoshaUser getById(long id) {
> 		// 增加对象级缓存
> 		// 取缓存
> 		MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
> 		if (user != null) return user;
> 		// 缓存中找不到，去数据库查找
> 		user = miaoshaUserDao.getById(id);
> 		if (user != null){
> 			redisService.set(MiaoshaUserKey.getById,""+id,user);
> 		}
> 		return user;
> 	}
> ```
>
> 页面静态化：将页面直接缓存到用户浏览器
>
> ```html
> #static
> spring.resources.add-mappings=true
> spring.resources.cache-period= 3600
> spring.resources.chain.cache=true 
> spring.resources.chain.enabled=true
> spring.resources.chain.gzipped=true
> spring.resources.chain.html-application-cache=true
> spring.resources.static-locations=classpath:/static/
> ```
>
> 

1. 在使用页面静态缓存的时候，缓存进redis的页面静态代码是正确的，但是再次读出时就会出错。

   原因：在第一次读取页面静态代码时会将页面代码写入redis，而在设置redis值时会使用到beanToString()函数，最开始的beanToString（）代码如下：

   ```java
   private <T> String beanToString(T value) {
   //		if (value == null) return null;
   //		Class<?> clazz = value.getClass();
   //		if (clazz == int.class || clazz == Integer.class){
   //			return ""+value;
   //		}else if(clazz == String.class){
   //			return (String)value;
   //		}else if (clazz == long.class || clazz == Long.class){
   //			return  ""+value;
   //		}
   		return JSON.toJSONString(value);
   	}
   ```

   只是仅仅返回了toJSONString，而页面缓存输入的是String类型，这么做有失妥当。正确的做法打开注释部分。

   > 页面缓存和URL缓存属于粗粒度缓存，缓存时间比较短。对于分页情况，页面缓存也只是会缓存前一两页结果，并不会全部都缓存。（大部分用户点击一两页足矣）

   > 页面静态化：将页面直接缓存到用户的浏览器上
   >
   > 1. 常用技术 AngularJS、Vue.JS
   > 2. 优点：利用浏览器的缓存



> 问：如果一个用户在多个设备上登录，并同时发出秒杀请求，则可能会出现一个用户秒杀到多个商品。如何解决？
>
> 答：思路一：项目中有一个专门保存秒杀订单的表"miaosha_order"，在秒杀订单上创建唯一索引可以解决上述问题（具体索引是user_id和goods_id）

> 静态资源优化：
>
> - JS/CSS压缩，减少流量
> - 多个JS/CSS组合，减少连接数
> - CDN就近访问 

答：1. JS/CSS 压缩，减少流量

  		2. 多个JS/CSS组合，减少连接数
    		3. CDN就近访问



### 第六章 接口优化

1. Redis预减库存减少数据库访问
2. 内存标记减少对Redis访问
3. 请求先入队缓冲，异步下单，增强用户体验
4. RabbitMQ安装与SpringBoot集成
5. Nginx水平拓展
6. 压测

> 在第五章如何解决超卖？
>
> - 数据库加唯一索引：防止用户重复购买
> - SQL加库存数量判断：防止库存变成负数



如何优化  秒杀接口？

思路：减少数据库访问

1. 系统初始化，把商品库存数量加载到Redis

> 实现：让MiaoshaController 实现InitialingBean接口，并重写afterPropertiesSet（）方法。
>
> ```java
> @Override
> 	public void afterPropertiesSet() throws Exception {
> 		List<GoodsVo> goodsList = goodsService.listGoodsVo();
> 		if (goodsList == null) {
> 			return;
> 		}
>     // 将商品信息全部写入redis
> 		for (GoodsVo goods : goodsList) {
> 			redisService.set(GoodsKey.getMiaoshaGoodsStock, goods.getId() + "", goods.getStockCount());
> 			localOverMap.put(goods.getId(),false);
> 		}
> 	}
> ```
>
> 

2. 收到请求，Redis预减库存，库存不足，直接返回，否则进入3

> Redis自减操作具有原子性，可应对并发请求。
>
> 如果库存不足，则直接返回。这一步可以省去对数据库的访问
>
> ```java
> // 预减库存
> 		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, goodsId + "");
> 		if (stock < 0) {
> 			localOverMap.put(goodsId,true);
> 			return Result.error(CodeMsg.MIAO_SHA_OVER);
> 		}
> ```
>
> 

3. 请求入队，立即返回排队中

   > 同步下单变为异步下单，增加用户体验
   >
   > 这里使用四种模式中最简单的direct模式
   >
   > ```java
   > // 入队
   > 		MiaoshaMessage mm = new MiaoshaMessage();
   > 		mm.setUser(user);
   > 		mm.setGoodsId(goodsId);
   > 		mqSender.sendMiaoshaMessage(mm);
   > ```
   >
   > 

4. 请求出队，生成订单，减少库存

   > 流程是：减库存，下订单，写入秒杀订单。需要注意，最后生成的秒杀订单会写入到redis中去，方便查询用户是否重复下单
   >
   > 优化：在miaosha（）方法中，一旦发现某一个商品库存为0，则在Redis中设置一个标志位，表示该商品已经售罄。
   >
   > ```java
   > // 优化：内存标记，减少对Redis的访问
   > // 设置某一商品是否秒杀完。如果某一时间检测发现该商品库存为0，则put(goodsId, true);
   > // 以后到来的请求只会先去检查该map。如果发现自己要秒杀的goodId没库存了，就不会进行以后的操作，
   > // 可以减少对Redis的访问
   > private Map<Long,Boolean> localOverMap = new HashMap<>();
   > ```
   >
   > 

5. 客户端轮询，是否秒杀成功



### 第七章  安全优化

1. 秒杀接口地址隐藏
2. 数学公式验证码
3. 接口限流防刷



#### 秒杀接口地址隐藏

思路：秒杀开始之前，先去请求接口获取秒杀地址

1. 接口改造，带上PathVariable参数
2. 添加生成地址的接口
3. 秒杀收到请求，先验证PathVariable

> path是一段随机生成的UUID
>
> 也就是将do_miaosha接口改造为/{path}/do_miaosha，其中的path是服务端生成，存储在redis中并返回给前端的。前端带着path再次去请求do_miaosha接口，此时服务器会首先验证path的合法性。这样在秒杀开始前，恶意用户即使知道了秒杀接口，也无法构造do_miaosha请求。注意，秒杀按钮只要在秒杀时间段才能点击。在秒杀开始之前，无法点击秒杀按钮，仅能看到 按钮事件发送了/miaosha/path请求。



#### 数学公式验证码

思路：点击秒杀之前，先输入验证码，分散用户请求

1. 添加生成验证码的接口
2. 在获取秒杀路径的时候，验证验证码
3. ScriptEngine使用

> 每次生成验证码之后，就将验证码结果拼装userId存入redis中。在用户输入验证码之后，将redis中的验证码取出并进行校验



#### 接口防刷

思路：对接口做限流

通过拦截器减少对代码的侵入

> 限流实现：每个用户到来之后，我们就在reids中存储userId+访问接口的key。核心代码如下：
>
> ```java
> AccessKey ak = AccessKey.withExpire(second);
> 			Integer count = redisService.get(ak, key, Integer.class);
> 			if (count == null){ // 拿不到，说明用户第一次访问，将userID+接口 放入redis（带失效时间）
> 				redisService.set(ak,key,1);
> 			}else if (count < maxCount){// 如果次数小于maxCount，就将key对应的value+1
> 				redisService.incr(ak,key);// redis中的incr是原子操作
> 			}else { // 否则说明用户访问次数超过限制，返回失败
> 				render(response,CodeMsg.ACCESS_LIMIT);
> 				return false;
> 			}
> ```
>
> 通过拦截器优化：
>
> 考虑到不同的访问接口限流频率要求不同，如果为每一个接口都设定maxCount和过期时间second，则会产生大量冗余代码，故采用拦截器优化：
>
> 自定义注解：@AccessLimit:
>
> ```java
> public @interface AccessLimit {
>    int second();
> 
>    int maxCount();
> 
>    boolean needLogin() default true;
> }
> ```
>
> 自定义拦截器：
>
> ```java
> @Component
> public class AccessInterceptor extends HandlerInterceptorAdapter {
> 
>    @Autowired
>    MiaoshaUserService userService;
> 
>    @Autowired
>    RedisService redisService;
> 
>    @Override
>    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
>       if (handler instanceof HandlerMethod){
>          MiaoshaUser user = getUser(request, response);
>          UserContext.setUser(user);
> 
>          HandlerMethod hm = (HandlerMethod) handler;
>          AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
>          if (accessLimit == null){
>             return true;
>          }
>          int second = accessLimit.second();
>          int maxCount = accessLimit.maxCount();
>          boolean needLogin = accessLimit.needLogin();
>          String key = request.getRequestURI();
>          if (needLogin){
>             if (user == null){
>                render(response, CodeMsg.SESSION_ERROR);
>                return false;
>             }
>             key += "_" + user.getId();
>          }else {
>             // do nothing
>          }
> 
>          // 对每一个用户生成访问key，设置时间。在固定时间间隔内用户的访问次数是受限的
>          AccessKey ak = AccessKey.withExpire(second);
>          Integer count = redisService.get(ak, key, Integer.class);
>          if (count == null){
>             redisService.set(ak,key,1);
>          }else if (count < maxCount){
>             redisService.incr(ak,key);
>          }else {
>             render(response,CodeMsg.ACCESS_LIMIT);
>             return false;
>          }
>       }
>       return super.preHandle(request, response, handler);
>    }
> 
>    private void render(HttpServletResponse response, CodeMsg sessionError) throws IOException {
>       response.setContentType("application/json;charSet=UTF-8");
>       ServletOutputStream out = response.getOutputStream();
>       String str = JSON.toJSONString(Result.error(sessionError));
>       out.write(str.getBytes());
>       out.flush();
>       out.close();
>    }
> 
>    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){
>       String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
>       String cookieToken = getCookieValue(request,MiaoshaUserService.COOKIE_NAME_TOKEN);
>       if (StringUtils.isEmpty(paramToken) && StringUtils.isEmpty(cookieToken)){
>          return null;
>       }
>       String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
>       return userService.getByToken(response,token);
>    }
> 
>    private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
>       Cookie[] cookies = request.getCookies();
>       if (cookies == null) return null;
>       for (Cookie cookie : cookies){
>          if (cookie.getName().equals(cookieNameToken)){
>             return cookie.getValue();
>          }
>       }
>       return null;
>    }
> }
> ```

