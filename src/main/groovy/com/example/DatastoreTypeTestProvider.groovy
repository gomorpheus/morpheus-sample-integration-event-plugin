package com.example

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.providers.DatastoreTypeProvider
import com.morpheusdata.model.AccountIntegration
import com.morpheusdata.model.ComputeServer
import com.morpheusdata.model.ComputeServerGroup
import com.morpheusdata.model.Datastore
import com.morpheusdata.model.OptionType
import com.morpheusdata.model.Snapshot
import com.morpheusdata.model.StorageVolume
import com.morpheusdata.model.VirtualImage
import com.morpheusdata.model.event.DatastoreEvent
import com.morpheusdata.model.event.EventType
import com.morpheusdata.response.ServiceResponse
import com.morpheusdata.core.providers.ProvisionProvider
import com.morpheusdata.model.DatastoreType
import com.morpheusdata.model.ProvisionType
import com.morpheusdata.model.StorageServerType
import com.morpheusdata.model.StorageServer
import groovy.util.logging.Slf4j

@Slf4j
class DatastoreTypeTestProvider implements DatastoreTypeProvider, DatastoreTypeProvider.DatastoreEventFacet {

	public static final String DATASTORE_TYPE_PROVIDER_CODE = 'morpheus-datastore-test-plugin.datastore'

	protected MorpheusContext context
	protected Plugin plugin

	public DatastoreTypeTestProvider(Plugin plugin, MorpheusContext ctx) {
		super()
		this.@context = ctx
		this.@plugin = plugin
	}
	/**
	 * Returns the {@link ProvisionProvider} code for linking the generated {@link DatastoreType} with the appropriate {@link ProvisionType}
	 * @return the code of the relevant ProvisionProvider
	 */
	@Override
	String getProvisionTypeCode() {
		return 'morpheus-datastore-test-plugin.provision'
	}

	/**
	 * Returns the provider code for interacting with the {@link StorageServer} interface
	 * This is optional and can be null if there is no interaction with a storage server whatsoever
	 * @return the code for the storage provider (also matches the {@link StorageServerType} code)
	 */
	@Override
	String getStorageProviderCode() {
		return 'morpheus-datastore-test-plugin.storage'
	}

	/**
	 * Provide custom configuration options when creating a new {@link AccountIntegration}
	 * @return a List of OptionType
	 */
	@Override
	List<OptionType> getOptionTypes() {
		Collection<OptionType> options = []
		// TODO: create some option types for the datastore and add them to collection
		return options
	}

	/**
	 * Flags if this datastore can be created by the user. Some datastores are system injected and cannot be created by the user
	 * @return whether, or not this datastore can be created by the user
	 */
	@Override
	boolean getCreatable() {
		return true
	}

	/**
	 * Flags if the datastore created for this is editable or not
	 * @return whether, or not this datastore can be edited once added
	 */
	@Override
	boolean getEditable() {
		return true
	}

	/**
	 * Flags if the datastore created for this is removable or not
	 * @return whether, or not this datastore can be removed once added
	 */
	@Override
	boolean getRemovable() {
		return true
	}

	/**
	 * Perform any operations necessary on the target to remove a volume. This is used to remove a volume on a storage server
	 * It is typically called as part of server teardown.
	 * @param volume the current volume to remove
	 * @param server the server the volume is being removed from (may contain information such as parentServer (hypervisor) or cluster)
	 * @param removeSnapshots whether to remove snapshots associated with the volume. In some implementations this is mandatory and not separate.
	 * @param force whether to force the removal of the volume. This is typically used to force the removal of a volume that is in use.
	 * @return the success state of the removal
	 */
	@Override
	ServiceResponse removeVolume(StorageVolume volume, ComputeServer server, boolean removeSnapshots, boolean force) {
		return ServiceResponse.success()
	}

	/**
	 * Perform any operations necessary on the target to create a volume. This is used to create a volume on a storage server
	 * It is typically called as part of server provisioning.
	 * @param volume the current volume to create
	 * @param server the server the volume is being created on (may contain information such as parentServer (hypervisor) or cluster)
	 * @return the success state and a copy of the volume
	 */
	@Override
	ServiceResponse<StorageVolume> createVolume(StorageVolume volume, ComputeServer server) {
		return ServiceResponse.success()
	}

	/**
	 * Clones a volume based on a source volume object. This is one of the most important methods for provisioning as most
	 * {@link VirtualImage} provisioning objects are cloned from a local image cache of source volumes. This is where the QCOW2 may reside
	 * Often times you can infer this from the combination of the sourceVolume object as well as its datastore
	 * <p>
	 *     <code>
	 *         String sourceVolumePath = sourceVolume.datastore.externalPath + '/' + sourceVolume.externalId
	 *         //this is the QCOW2 path typically used for clone operations
	 *         String command = "sudo mkdir -p \"${volume.datastore.externalPath}/${server.externalId ?: server.name}\" ; sudo ionice -c 3 cp -f \"${sourceVolume.datastore.externalPath}/${sourceVolume.externalId}\" \"${volume.datastore.externalPath}/${server.externalId ?: server.name}/${volume.volumeName}\""
	 * 		   morpheusContext.executeCommandOnServer(server, command)
	 *     </code>
	 * </p>
	 * @param volume the volume we are creating and cloning into
	 * @param server the server the volume is associated with (typically the workload/vm)
	 * @param sourceVolume the source volume we are cloning from
	 * @return the success state and a copy of the volume
	 */
	@Override
	ServiceResponse<StorageVolume> cloneVolume(StorageVolume volume, ComputeServer server, StorageVolume sourceVolume) {
		return ServiceResponse.success()
	}

	/**
	 * Perform any operations necessary on the target to resize a volume. This is used to resize a volume on a storage server
	 * @param volume the current volume to resize
	 * @param server the server the volume is being resized on (may contain information such as parentServer (hypervisor) or cluster)
	 * @param newSize the new size of the volume... TODO: this exists on the volume record already, is newSize needed?
	 * @return the success state and a copy of the volume
	 */
	@Override
	ServiceResponse<StorageVolume> resizeVolume(StorageVolume volume, ComputeServer server, Long newSize) {
		return ServiceResponse.success()
	}

	/**
	 * Perform any operations necessary on the target to create and register a datastore.
	 * Most implementations iterate over the servers on the server group (hypervisors) and register a storage pool
	 * @param datastore the current datastore being created
	 * @return the service response containing success state or any errors upon failure
	 */
	@Override
	ServiceResponse<Datastore> createDatastore(Datastore datastore) {
		log.info("DatastoreTestEventPluginDatastoreTypeProvider - create datastore called with datastore -- [id: ${datastore?.id}, name: ${datastore?.name}, type: ${datastore?.datastoreType?.code}]")

		return ServiceResponse.success()
	}

	@Override
	ServiceResponse<Datastore> updateDatastore(Datastore datastore) {
		log.info("DatastoreTestEventPluginDatastoreTypeProvider - update datastore called with datastore -- [id: ${datastore?.id}, name: ${datastore?.name}, type: ${datastore?.datastoreType?.code}]")
		return ServiceResponse.success(datastore);
	}


	/**
	 * Perform any operations necessary on the target to remove a datastore. this method should be implemented
	 * if {@link DatastoreTypeProvider#getRemovable()} is true. otherwise return null or an error.
	 * @param datastore the current datastore being removed
	 * @return the success state of the removal
	 */
	@Override
	ServiceResponse removeDatastore(Datastore datastore) {
		log.info("DatastoreTestEventPluginDatastoreTypeProvider - remove datastore called with datastore -- [id: ${datastore?.id}, name: ${datastore?.name}, type: ${datastore?.datastoreType?.code}]")

		return ServiceResponse.success()
	}

	/**
	 * Clones a volume based on a source being the reference to the actual File in the Virtual Image. This can be called in the event there is no image cache or we need to directly stream to an image target.
	 * Remember, this code runs in the manager or morpheus appliance and not on the host itself. In order to stream contents directly to the target , we need to create a link we can fetch using the {@link com.morpheusdata.core.MorpheusFileCopyService}
	 * @see com.morpheusdata.core.MorpheusFileCopyService* @see com.morpheusdata.core.synchronous.MorpheusSynchronousFileCopyService* @param volume the volume we are creating and cloning into
	 * @param server the server the volume is associated with (typically the workload/vm)
	 * @param virtualImage the virtual image this volume is being cloned out of
	 * @param cloudFile the specific disk file (Karman abstraction) that is being cloned
	 * @return the success state and a copy of the volume
	 */
	@Override
	ServiceResponse<StorageVolume> cloneVolume(StorageVolume volume, ComputeServer server, VirtualImage virtualImage, com.bertramlabs.plugins.karman.CloudFileInterface cloudFile) {
		return ServiceResponse.success()
	}

	/**
	 * Returns the Morpheus Context for interacting with data stored in the Main Morpheus Application
	 *
	 * @return an implementation of the MorpheusContext for running Future based rxJava queries
	 */
	@Override
	MorpheusContext getMorpheus() {
		return this.@context
	}

	/**
	 * Returns the instance of the Plugin class that this provider is loaded from
	 * @return Plugin class contains references to other providers
	 */
	@Override
	Plugin getPlugin() {
		return this.@plugin
	}

	/**
	 * A unique shortcode used for referencing the provided provider. Make sure this is going to be unique as any data
	 * that is seeded or generated related to this provider will reference it by this code.
	 * @return short code string that should be unique across all other plugin implementations.
	 */
	@Override
	String getCode() {
		return DATASTORE_TYPE_PROVIDER_CODE
	}

	/**
	 * Provides the provider name for reference when adding to the Morpheus Orchestrator
	 * NOTE: This may be useful to set as an i18n key for UI reference and localization support.
	 *
	 * @return either an English name of a Provider or an i18n based key that can be scanned for in a properties file.
	 */
	@Override
	String getName() {
		return 'Datastore Test Event Plugin Datastore'
	}


	@Override
	void onEvent(DatastoreEvent datastoreEvent, AccountIntegration accountIntegration) {
		log.info("VERSION 1 -- Datastore Test Event Plugin Datastore - onEvent called with event: ${datastoreEvent.type} and accountIntegration: ${accountIntegration?.id}, datastore: ${datastoreEvent.datastore?.id}, server: ${datastoreEvent.server?.id}, sourceHost: ${datastoreEvent.sourceHost?.id}, targetHost: ${datastoreEvent.targetHost?.id}")
	}

	@Override
	List<EventType> getSupportedEventTypes() {
		return [DatastoreEvent.DatastoreEventType.SERVER_MOVE, DatastoreEvent.DatastoreEventType.SERVER_SHUTDOWN, DatastoreEvent.DatastoreEventType.SERVER_STARTUP]
	}
}
