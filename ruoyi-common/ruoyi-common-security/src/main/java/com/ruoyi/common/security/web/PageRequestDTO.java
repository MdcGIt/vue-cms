package com.ruoyi.common.security.web;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequestDTO {

	/**
	 * 当前页码，从1开始
	 */
	private static final String GET_PARAM_PAGENUM = "pageNum";

	/**
	 * 每页条数，默认20
	 */
	private static final String GET_PARAM_PAGESIZE = "pageSize";

	/**
	 * 排序条件，格式：column1#desc@column2#asc
	 */
	private static final String GET_PARAM_SORTS = "sorts";

	/**
	 * 页码
	 */
	private int pageNum;

	/**
	 * 每页数量
	 */
	private int pageSize;

	/**
	 * 排序条件集合
	 */
	private List<SortOrder> sorts;

	/**
	 * 通过默认请求参数构造分页信息
	 * 
	 * GET: page=1&size=20&sort=col1#asc@col2#desc
	 * OTHERS: {page: 1, size: 20, sort: [{"column":"publishDate","direction":"DESC"}]}
	 */
	public static PageRequest buildPageRequest() {
		HttpServletRequest request = ServletUtils.getRequest();
		if (RequestMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
			int page = ServletUtils.getParameterToInt(request, GET_PARAM_PAGENUM, 1);
			int size = ServletUtils.getParameterToInt(request, GET_PARAM_PAGESIZE, 20);

			String sortStr = ServletUtils.getParameter(GET_PARAM_SORTS);
			Sort sort = Sort.unsorted();
			if (StringUtils.isNotEmpty(sortStr)) {
				List<Order> orders = StringUtils.splitToMap(sortStr, "@", "#").entrySet().stream().map(e -> {
					Optional<Direction> opt = Sort.Direction.fromOptionalString(e.getValue());
					if (opt.isPresent()) {
						return new Sort.Order(opt.get(), e.getKey());
					}
					return null;
				}).filter(Objects::nonNull).toList();
				sort = Sort.by(orders);
			}
			return PageRequest.of(page, size, sort);
		} else {
			try {
				PageRequestDTO dto = JacksonUtils.from(request.getInputStream(), PageRequestDTO.class);
				List<Order> orders = dto.getSorts().stream()
						.map(order -> {
							if (Direction.ASC.equals(order.getDirection())) {
								return Sort.Order.asc(order.getColumn());
							} else if (Direction.DESC.equals(order.getDirection())) {
								return Sort.Order.desc(order.getColumn());
							}
							return null;
						}).filter(Objects::nonNull).toList();
				return PageRequest.of(dto.getPageNum(), dto.getPageSize(), Sort.by(orders));
			} catch (IOException e) {
				e.printStackTrace();
				return PageRequest.of(1, 20);
			}
		}
	}

	public static PageRequest getPageRequest() {
		HttpServletRequest request = ServletUtils.getRequest();
		if (RequestMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
			int page = ServletUtils.getParameterToInt(request, GET_PARAM_PAGENUM, 1);
			int size = ServletUtils.getParameterToInt(request, GET_PARAM_PAGESIZE, 20);

			String sortStr = ServletUtils.getParameter(GET_PARAM_SORTS);
			// 将单引号替换为双引号
			sortStr = sortStr.replace('\'', '\"');
			Sort sort = Sort.unsorted();
			if (StringUtils.isNotEmpty(sortStr)) {
				List<SortOrder> sortOrderList = List.of();
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					 sortOrderList = objectMapper.readValue(sortStr, new TypeReference<List<SortOrder>>(){});
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
				List<Order> orders = sortOrderList.stream().map(e -> new Order(e.getDirection(), e.getColumn())).filter(Objects::nonNull).toList();
				sort = Sort.by(orders);
			}
			return PageRequest.of(page, size, sort);
		} else {
			try {
				PageRequestDTO dto = JacksonUtils.from(request.getInputStream(), PageRequestDTO.class);
				List<Order> orders = dto.getSorts().stream()
						.map(order -> {
							if (Direction.ASC.equals(order.getDirection())) {
								return Sort.Order.asc(order.getColumn());
							} else if (Direction.DESC.equals(order.getDirection())) {
								return Sort.Order.desc(order.getColumn());
							}
							return null;
						}).filter(Objects::nonNull).toList();
				return PageRequest.of(dto.getPageNum(), dto.getPageSize(), Sort.by(orders));
			} catch (IOException e) {
				e.printStackTrace();
				return PageRequest.of(1, 20);
			}
		}
	}

	@Getter
	@Setter
	public static class SortOrder {

		/**
		 * 排序字段
		 */
		private String column;

		/**
		 * 升序/降序，默认：升序
		 */
		private Direction direction = Direction.ASC;
	}
}
