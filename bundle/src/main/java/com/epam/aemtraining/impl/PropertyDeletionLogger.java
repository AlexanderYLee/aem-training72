package com.epam.aemtraining.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 *
 * Created by Aliaksandr_Li on 5/16/2016.
 */
@Component
@Service(value={JobConsumer.class})
@Property(name=JobConsumer.PROPERTY_TOPICS, value="com/epam/aemtraining72/job")
public class PropertyDeletionLogger implements JobConsumer{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private Session session;
    @Reference
    SlingRepository repository;
    public JobResult process(Job job) {
        try {
            session = repository.loginAdministrative(null);
            Node rootNode = session.getRootNode();
            String propertyPath = job.getProperty("path", String.class);
            String[] propertyNames = job.getProperty("names", String[].class);
            for (String name: propertyNames){
                String path = String.format("var/log/removedProperties/%s", job.getId());
                Node node = null;
                if (rootNode.hasNode(path)){
                    node = rootNode.getNode(path);
                }else{
                    node = addNode(rootNode, path);
                    if (node!=null){
                        node.setProperty("path", propertyPath);
                    }else
                        return JobResult.FAILED;
                }
                if (node!=null){
                    node.addNode(name).setPrimaryType("nt:unstructured");
                }else
                    return JobResult.FAILED;
            }
            session.save();
            return JobResult.OK;
        } catch (RepositoryException e) {
            log.debug(e.getMessage());
        }finally {
            session.logout();
        }
        return JobResult.FAILED;
    }

    private Node addNode(Node rootNode, String path){
        Node node = rootNode;
        for (String item: path.split("/")){
            try {
                if (!node.hasNode(item)){
                    node = node.addNode(item);
                    node.setPrimaryType("sling:Folder");
                }else{
                    node = node.getNode(item);
                }
            } catch (RepositoryException e) {
                log.debug(e.getMessage());
                return null;
            }
        }
        return node;
    }
}
