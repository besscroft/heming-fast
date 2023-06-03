package dev.heming.fast.param.resourceType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description 资源类别更新参数
 * @Author Bess Croft
 * @Date 2023/5/29 16:30
 */
@Data
@Schema(title = "资源类别更新参数")
public class ResourceTypeUpdateParam {

    @Schema(title = "id", type = "Long", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(title = "名称", type = "String", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(title = "编码", type = "String", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编码不能为空")
    private String code;

    @Schema(title = "描述", type = "String")
    private String description;

    @Schema(title = "排序", type = "Integer")
    private Integer sort;

}
