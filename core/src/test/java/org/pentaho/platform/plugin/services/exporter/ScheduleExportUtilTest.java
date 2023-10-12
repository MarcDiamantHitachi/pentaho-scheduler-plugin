/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.exporter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.ICronJobTrigger;
import org.pentaho.platform.api.scheduler2.IJob;
import org.pentaho.platform.api.scheduler2.IJobScheduleParam;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.ISimpleJobTrigger;
import org.pentaho.platform.web.http.api.resources.JobScheduleParam;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class ScheduleExportUtilTest {

  @Before
  public void setUp() throws Exception {

  }

  @Test( expected = IllegalArgumentException.class )
  public void testCreateJobScheduleRequest_null() throws Exception {
    ScheduleExportUtil.createJobScheduleRequest( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testCreateJobScheduleRequest_unknownTrigger() throws Exception {
    String jobName = "JOB";

    IJob job = mock( IJob.class );
    IJobTrigger trigger = mock( IJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );

  }

  @Test
  public void testCreateJobScheduleRequest_SimpleJobTrigger() throws Exception {
    String jobName = "JOB";

    IJob job = mock( IJob.class );
    ISimpleJobTrigger trigger = mock( ISimpleJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );
    assertEquals( trigger, jobScheduleRequest.getSimpleJobTrigger() );
  }

  @Test
  public void testCreateJobScheduleRequest_NoStreamProvider() throws Exception {
    String jobName = "JOB";

    IJob job = mock( IJob.class );
    ISimpleJobTrigger trigger = mock( ISimpleJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    Map<String, Serializable> params = new HashMap<>();
    params.put( "directory", "/home/admin" );
    params.put( "transformation", "myTransform" );

    HashMap<String, String> pdiParams = new HashMap<>();
    pdiParams.put( "pdiParam", "pdiParamValue" );
    params.put( ScheduleExportUtil.RUN_PARAMETERS_KEY, pdiParams );

    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );
    assertEquals( trigger, jobScheduleRequest.getSimpleJobTrigger() );
    assertEquals( "/home/admin/myTransform.ktr", jobScheduleRequest.getInputFile() );
    assertEquals( "/home/admin/myTransform*", jobScheduleRequest.getOutputFile() );
    assertEquals( "pdiParamValue", jobScheduleRequest.getPdiParameters().get( "pdiParam" ) );
  }

  @Test
  public void testCreateJobScheduleRequest_StringStreamProvider() throws Exception {
    String jobName = "JOB";

    IJob job = mock( IJob.class );
    ISimpleJobTrigger trigger = mock( ISimpleJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    Map<String, Serializable> params = new HashMap<>();
    params.put( IScheduler.RESERVEDMAPKEY_STREAMPROVIDER, "import file = /home/admin/myJob.kjb:output file=/home/admin/myJob*" );
    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );
    assertEquals( trigger, jobScheduleRequest.getSimpleJobTrigger() );
    assertEquals( "/home/admin/myJob.kjb", jobScheduleRequest.getInputFile() );
    assertEquals( "/home/admin/myJob*", jobScheduleRequest.getOutputFile() );
  }

  @Test
  public void testCreateJobScheduleRequest_ComplexJobTrigger() throws Exception {
    String jobName = "JOB";
    Date now = new Date();

    IJob job = mock( IJob.class );
    IComplexJobTrigger trigger = mock( IComplexJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );

    when( trigger.getCronString() ).thenReturn( "0 30 13 ? * 2,3,4,5,6 *" );
    when( trigger.getDuration() ).thenReturn( -1L );
    when( trigger.getStartTime() ).thenReturn( now );
    when( trigger.getEndTime() ).thenReturn( now );
    when( trigger.getUiPassParam() ).thenReturn( "uiPassParm" );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );

    // we should be getting back a cron trigger, not a complex trigger.
    assertNull( jobScheduleRequest.getSimpleJobTrigger() );
    assertNull( jobScheduleRequest.getComplexJobTrigger() );
    assertNotNull( jobScheduleRequest.getCronJobTrigger() );

    assertEquals( trigger.getCronString(), jobScheduleRequest.getCronJobTrigger().getCronString() );
    assertEquals( trigger.getDuration(), jobScheduleRequest.getCronJobTrigger().getDuration() );
    assertEquals( trigger.getEndTime(), jobScheduleRequest.getCronJobTrigger().getEndTime() );
    assertEquals( trigger.getStartTime(), jobScheduleRequest.getCronJobTrigger().getStartTime() );
    assertEquals( trigger.getUiPassParam(), jobScheduleRequest.getCronJobTrigger().getUiPassParam() );
  }

  @Test
  public void testCreateJobScheduleRequest_CronJobTrigger() throws Exception {
    String jobName = "JOB";

    IJob job = mock( IJob.class );
    ICronJobTrigger trigger = mock( ICronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );
    assertEquals( trigger, jobScheduleRequest.getCronJobTrigger() );
  }

  @Test
  public void testCreateJobScheduleRequest_StreamProviderJobParam() throws Exception {
    String jobName = "JOB";
    String inputPath = "/input/path/to/file.ext";
    String outputPath = "/output/path/location.*";

    Map<String, Serializable> params = new HashMap<>();

    RepositoryFileStreamProvider streamProvider = mock( RepositoryFileStreamProvider.class );
    params.put( IScheduler.RESERVEDMAPKEY_STREAMPROVIDER, streamProvider );

    IJob job = mock( IJob.class );
    ICronJobTrigger trigger = mock( ICronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    when( job.getJobParams() ).thenReturn( params );
    when( streamProvider.getInputFilePath() ).thenReturn( inputPath );
    when( streamProvider.getOutputFilePath() ).thenReturn( outputPath );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );
    assertEquals( inputPath, jobScheduleRequest.getInputFile() );
    assertEquals( outputPath, jobScheduleRequest.getOutputFile() );
    assertEquals( 0, jobScheduleRequest.getJobParameters().size() );
  }

  @Test
  public void testCreateJobScheduleRequest_ActionClassJobParam() throws Exception {
    String jobName = "JOB";
    String actionClass = "com.pentaho.Action";
    Map<String, Serializable> params = new HashMap<>();

    params.put( IScheduler.RESERVEDMAPKEY_ACTIONCLASS, actionClass );

    IJob job = mock( IJob.class );
    ICronJobTrigger trigger = mock( ICronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );
    assertEquals( actionClass, jobScheduleRequest.getActionClass() );
    assertEquals( actionClass, ( (IJobScheduleParam) jobScheduleRequest.getJobParameters().get( 0 ) ).getValue() );
  }

  @Test
  public void testCreateJobScheduleRequest_TimeZoneJobParam() throws Exception {
    String jobName = "JOB";
    String timeZone = "America/New_York";
    Map<String, Serializable> params = new HashMap<>();

    params.put( IBlockoutManager.TIME_ZONE_PARAM, timeZone );

    IJob job = mock( IJob.class );
    ICronJobTrigger trigger = mock( ICronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );
    assertEquals( timeZone, jobScheduleRequest.getTimeZone() );
    assertEquals( timeZone, ( (IJobScheduleParam) jobScheduleRequest.getJobParameters().get( 0 ) ).getValue() );
  }

  @Test
  public void testCreateJobScheduleRequest_MultipleTypesJobParam() throws Exception {
    String jobName = "JOB";
    Long l = Long.MAX_VALUE;
    Date d = new Date();
    Boolean b = true;

    Map<String, Serializable> params = new HashMap<>();

    params.put( "NumberValue", l );
    params.put( "DateValue", d );
    params.put( "BooleanValue", b );

    IJob job = mock( IJob.class );
    ICronJobTrigger trigger = mock( ICronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) ScheduleExportUtil.createJobScheduleRequest( job );
    List <IJobScheduleParam> jobScheduleParams = (ArrayList<IJobScheduleParam>)(ArrayList<?>) jobScheduleRequest.getJobParameters();
    for ( IJobScheduleParam jobScheduleParam : jobScheduleParams ) {
      assertTrue( jobScheduleParam.getValue().equals( l )
              || jobScheduleParam.getValue().equals( d )
              || jobScheduleParam.getValue().equals( b ) );
    }
  }

  @Test
  public void testConstructor() throws Exception {
    // only needed to get 100% code coverage
    assertNotNull( new ScheduleExportUtil() );
  }
}
