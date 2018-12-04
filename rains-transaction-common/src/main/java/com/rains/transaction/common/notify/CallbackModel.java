
package com.rains.transaction.common.notify;



import lombok.Data;

import java.io.Serializable;


@Data
public class CallbackModel implements Serializable {

    private String groupId;

    private String modelName;

    private String modelDomain;



}
