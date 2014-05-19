// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis;

import javax.resource.cci.*;

/**
 * <p>The <code>RecordConverter</code> interface allows conversion of an adapter 
 * specific record.  This can be used with the <code>EISPlatform</code> to allow 
 * user code to convert between the JCA-CCI Record used by the adapter and 
 * TopLink.  This can also be used to convert a proprietary adatper record 
 * format or contents into XML, Mapped or Indexed data.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public interface RecordConverter {
    Record converterFromAdapterRecord(Record record);

    Record converterToAdapterRecord(Record record);
}